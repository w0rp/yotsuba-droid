package com.w0rp.yotsubadroid;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ChanHTML {
    private enum ContentType {
        UNKNOWN,
        PLAIN,
        SPOILER,
        QUOTE,
        DEADLINK,
        QUOTELINK
    }

    private static class Content {
        public static class LinkMatch {
            private @Nullable
            final String boardID;
            private final long threadID;
            private final long postID;

            public LinkMatch(@Nullable String boardID, long threadID,
            long postID) {
                this.boardID = boardID;
                this.threadID = threadID;
                this.postID = postID;
            }

            public @Nullable String getBoardID() {
                return boardID;
            }

            public long getThreadID() {
                return threadID;
            }

            public long getPostID() {
                return postID;
            }

            @Override
            public String toString() {
                return "board: "
                    + boardID
                    + ", thread: "
                    + threadID
                    + ", post: "
                    + postID;
            }
        }

        private final ContentType type;
        private final String text;
        private final @Nullable LinkMatch linkMatch;

        public Content(ContentType type, String text,
        @Nullable LinkMatch linkMatch) {
            this.type = type;
            this.text = text;
            this.linkMatch = linkMatch;
        }

        public String getText() {
            return text;
        }

        public ContentType getType() {
            return type;
        }

        public @Nullable LinkMatch getLinkMatch() {
            return linkMatch;
        }

        @Override
        public String toString() {
            return "[" + type + ": " + text + "]" + linkMatch;
        }
    }

    private static class TagHandler extends DefaultHandler {
        private static final Pattern boardPattern =
            Pattern.compile("^/([a-zA-Z0-9_]+)");
        private static final Pattern threadPattern =
            Pattern.compile("/(\\d+)#p");
        private static final Pattern postPattern =
            Pattern.compile("#p(\\d+)$");

        final List<Content> contentList;
        ContentType state = ContentType.PLAIN;
        @Nullable Content.LinkMatch currentLinkMatch;

        public TagHandler() {
            contentList = new ArrayList<Content>();
        }

        public void add(ContentType type, String text) {
            contentList.add(new Content(type, text, currentLinkMatch));
        }

        @Override
        public void startElement(@Nullable String uri, @Nullable String name,
        @Nullable String qName, @Nullable Attributes atts) {
            assert uri != null;
            assert name != null;
            assert qName != null;
            assert atts != null;

            state = ContentType.PLAIN;
            currentLinkMatch = null;

            if (name.equals("br")) {
                add(ContentType.PLAIN, "\n");
            } else if (name.equals("s")) {
                state = ContentType.SPOILER;
            } else if (name.equals("span")) {
                String spanClass = atts.getValue("class");

                if ("quote".equals(spanClass)) {
                    state = ContentType.QUOTE;
                } else if ("deadlink".equals(spanClass)) {
                    state = ContentType.DEADLINK;
                } else {
                    state = ContentType.UNKNOWN;
                }
            } else if (name.equals("a")) {
                String aClass = atts.getValue("class");

                if ("quotelink".equals(aClass)) {
                    state = ContentType.QUOTELINK;

                    String href = atts.getValue("href");

                    List<String> boardMatches = Util.search(boardPattern, href);
                    List<String> threadMatches = Util.search(
                        threadPattern, href
                    );
                    List<String> postMatches = Util.search(postPattern, href);

                    String boardID = null;
                    long postID = 0;
                    long threadID = 0;

                    if (!boardMatches.isEmpty()) {
                        boardID = boardMatches.get(1)
                            .toLowerCase(Locale.ENGLISH);
                    }

                    if (!threadMatches.isEmpty()) {
                        threadID = Long.parseLong(threadMatches.get(1));
                    }

                    if (!postMatches.isEmpty()) {
                        postID = Long.parseLong(postMatches.get(1));
                    }

                    if (boardID != null || postID != 0) {
                        currentLinkMatch = new Content.LinkMatch(
                            boardID, threadID, postID
                        );
                    }
                }
            } else {
                state = ContentType.UNKNOWN;
            }
        }

        @Override
        public void endElement(
        @Nullable String uri, @Nullable String name, @Nullable String qName) {
            state = ContentType.PLAIN;
        }

        @Override
        public void characters(
        @Nullable char[] charList, int start, int length) {
            if (charList == null) {
                return;
            }

            String content = new String(charList, start, length);

            if (content.length() == 0) {
                return;
            }

            add(state, content);
        }
    }

    public interface QuotelinkClickHandler {
        void onQuotelinkClick(Post post, @Nullable String boardID, long threadID,
            long postID);
    }

    private static final QuotelinkClickHandler NULL_QUOTE_HANDLER =
    new QuotelinkClickHandler() {
        @Override
        public void onQuotelinkClick(Post post, @Nullable String boardID, long threadID,
        long postID) {}
    };

    public static final class TextGenerator {
        private class SpoilerSpan extends ClickableSpan {
            private final int spoilerIndex;

            public SpoilerSpan(final int spoilerIndex) {
                this.spoilerIndex = spoilerIndex;
            }

            @Override
            public void onClick(@Nullable View widget) {
                if (spoilerRevealSet.contains(spoilerIndex)) {
                    spoilerRevealSet.remove(spoilerIndex);
                } else {
                    spoilerRevealSet.add(spoilerIndex);
                }

                styleText();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);

                ds.setUnderlineText(false);

                if (spoilerRevealSet.contains(spoilerIndex)) {
                    ds.setColor(SPOILER_FG);
                } else {
                    ds.setColor(SPOILER_BG);
                }
            }
        }

        private class QuoteLinkSpan extends ClickableSpan {
            private final Content content;

            public QuoteLinkSpan(Content content) {
                this.content = content;
            }

            @Override
            public void onClick(@Nullable View widget) {
                final Content.LinkMatch linkMatch = content.getLinkMatch();

                if (linkMatch != null) {
                    quoteHandler.onQuotelinkClick(
                        post,
                        linkMatch.getBoardID(),
                        linkMatch.getThreadID(),
                        linkMatch.getPostID()
                    );
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);

                ds.setColor(QUOTELINK_COLOR);
            }
        }

        private final Set<Integer> spoilerRevealSet = new HashSet<Integer>();
        private final TextView textView;
        private final Post post;
        private final List<Content> contentList;
        private QuotelinkClickHandler quoteHandler = NULL_QUOTE_HANDLER;

        public TextGenerator(TextView textView, Post post) {
            assert(textView != null);
            assert(post != null);

            this.textView = textView;
            this.post = post;
            // We can parse everything up front, so we do it all once.
            this.contentList = parse(post.getComment());
        }

        public void setQuotelinkClickHandler(QuotelinkClickHandler handler) {
            this.quoteHandler = handler;
        }

        private Spanned generateSpan() {
            SpanBuilder sb = new SpanBuilder();

            int spoilerIndex = 0;
            boolean spoilerLast = false;

            for (Content content : contentList) {
                if (spoilerLast
                && content.getType() != ContentType.SPOILER) {
                    // We group spoilers together so we reveal a block at once.
                    ++spoilerIndex;
                    spoilerLast = false;
                }

                switch (content.getType()) {
                case PLAIN:
                case UNKNOWN:
                    sb.append(content.getText());
                break;
                case QUOTE:
                    sb.append(
                        content.getText(),
                        SpanBuilder.fg(QUOTE_COLOR)
                    );
                break;
                case DEADLINK:
                    sb.append(
                        content.getText(),
                        SpanBuilder.fg(QUOTELINK_COLOR),
                        SpanBuilder.strike()
                    );
                break;
                case QUOTELINK:
                    sb.append(
                        content.getText(),
                        new QuoteLinkSpan(content)
                    );
                break;
                case SPOILER: {
                    // Attach the spoiler span so we can tap spoilers to
                    // reveal them.
                    sb.append(
                        content.getText(),
                        SpanBuilder.bg(SPOILER_BG),
                        new SpoilerSpan(spoilerIndex)
                    );

                    spoilerLast = true;
                } break;
                }
            }

            return sb.span();
        }

        /**
         * Style text in the supplied text view.
         */
        public void styleText() {
            textView.setText(generateSpan());
        }
    }

    public static final int SPOILER_BG = Color.rgb(0, 0, 0);
    public static final int SPOILER_FG = Color.rgb(255, 255, 255);
    public static final int QUOTE_COLOR = Color.rgb(0, 255, 0);
    public static final int QUOTELINK_COLOR = Color.rgb(255, 0, 0);

    private static String xmlify(String html) {
        // TODO: Use smart Regex here.
        // The span replace here is here to fix some broken 4chan API
        // name field nonsense.
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root>"
            + html.replace("<br>", "<br />").replace("<wbr>", "")
            .replace("</span> <span class=\"commentpostername\">", " ")
            + "</root>";
    }

    private static List<Content> parse(String html) {
        TagHandler handler = new TagHandler();

        try {
            System.setProperty("org.xml.sax.driver",
                "org.xmlpull.v1.sax2.Driver");
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(handler);

            // Pass in the XML to parse.
            reader.parse(new InputSource(new StringReader(xmlify(html))));
        } catch (Exception e) {
            Log.e(ChanHTML.class.getName(), e.toString());
        }

        return handler.contentList;
    }

    public static @NonNull String rawText(String html) {
        StringBuilder sb = new StringBuilder();

        for (Content content : parse(html)) {
            if (content.getType() == ContentType.SPOILER) {
                // Skip spoilers in raw text.
                continue;
            }

            sb.append(content.getText());
        }

        return sb.toString();
    }
}
