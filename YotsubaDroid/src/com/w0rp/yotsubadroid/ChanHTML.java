package com.w0rp.yotsubadroid;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.w0rp.androidutils.RE;
import com.w0rp.androidutils.SLog;
import com.w0rp.androidutils.SpanBuilder;

import android.graphics.Color;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class ChanHTML {
    private static enum ContentType {
        UNKNOWN,
        PLAIN,
        SPOILER,
        QUOTE,
        DEADLINK,
        QUOTELINK
    }

    private static class Content {
        private ContentType type;
        private String text;
        private Map<String, Object> data;

        public Content(ContentType type, String text,
        Map<String, Object> data) {
            this.type = type;
            this.text = text;
            this.data = data;
        }

        public String getText() {
            return this.text;
        }

        public ContentType getType() {
            return this.type;
        }

        public Map<String, Object> getData() {
            if (this.data == null) {
                return Collections.emptyMap();
            }

            return Collections.unmodifiableMap(this.data);
        }

        @Override
        public String toString() {
            String out = "[" + this.type.toString() + ": " + this.text + "]";

            if (data != null) {
                out += data.toString();
            }

            return out;
        }
    }


    private static class TagHandler extends DefaultHandler {
        List<Content> contentList;
        ContentType state = ContentType.PLAIN;
        Map<String, Object> currentData;

        public TagHandler() {
            contentList = new ArrayList<Content>();
        }

        public void add(ContentType type, String text) {
            contentList.add(new Content(type, text, currentData));
        }

        public void startElement(String uri, String name, String qName,
            Attributes atts) {

            state = ContentType.PLAIN;
            currentData = null;

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

                    currentData = new HashMap<String, Object>();

                    String href = atts.getValue("href");

                    List<String> boardMatches
                        = RE.search("^/([a-zA-Z0-9_]+)", href);

                    if (!boardMatches.isEmpty()) {
                        currentData.put("boardID",
                            boardMatches.get(1).toLowerCase(Locale.ENGLISH));
                    }

                    List<String> postMatches
                        = RE.search("(\\d+)#[a-zA-z]?(\\d+)$", href);

                    if (!postMatches.isEmpty()) {
                        currentData.put("threadID",
                            Long.parseLong(postMatches.get(1)));
                        currentData.put("postID",
                            Long.parseLong(postMatches.get(2)));
                    }
                }
            } else {
                state = ContentType.UNKNOWN;
            }
        }

        public void endElement(String uri, String name, String qName) {
            state = ContentType.PLAIN;
        }

        public void characters(char charList[], int start, int length) {
            String content = new String(charList, start, length);

            if (content.length() == 0) {
                return;
            }

            add(state, content);
        }
    }

    public interface QuotelinkClickHandler {
        public void onQuotelinkClick(Post post, String boardID, long threadID,
            long postID);
    }

    public static final class TextGenerator {
        private class SpoilerSpan extends ClickableSpan {
            private final int spoilerIndex;

            public SpoilerSpan(final int spoilerIndex) {
                this.spoilerIndex = spoilerIndex;
            }

            @Override
            public void onClick(View widget) {
                if (spoilerRevealSet.contains(spoilerIndex)) {
                    spoilerRevealSet.remove(spoilerIndex);
                } else {
                    spoilerRevealSet.add(spoilerIndex);
                }

                styleText();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
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
            public void onClick(View widget) {
                if (quoteHandler != null) {
                    Map<String, Object> data = content.getData();

                    Long threadID = (Long) data.get("threadID");
                    Long postID = (Long) data.get("postID");

                    quoteHandler.onQuotelinkClick(post,
                        (String) data.get("boardID"),
                        threadID != null ? threadID : 0,
                        postID != null ? postID : 0);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);

                ds.setColor(QUOTELINK_COLOR);
            }
        }

        private final Set<Integer> spoilerRevealSet = new HashSet<Integer>();
        private final TextView textView;
        private final Post post;
        private final List<Content> contentList;
        private QuotelinkClickHandler quoteHandler;

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
                    sb.append(content.getText(),
                        SpanBuilder.fg(QUOTE_COLOR));
                break;
                case DEADLINK:
                    sb.append(content.getText(),
                        SpanBuilder.fg(QUOTELINK_COLOR),
                        SpanBuilder.strike());
                break;
                case QUOTELINK:
                    sb.append(content.getText(),
                        new QuoteLinkSpan(content));
                break;
                case SPOILER: {
                    // Attach the spoiler span so we can tap spoilers to
                    // reveal them.
                    sb.append(content.getText(),
                        SpanBuilder.bg(SPOILER_BG),
                        new SpoilerSpan(spoilerIndex));
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
            SLog.e(e);
        }

        return handler.contentList;
    }

    public static String rawText(String html) {
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
