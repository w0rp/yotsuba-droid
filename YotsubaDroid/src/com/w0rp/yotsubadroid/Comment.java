package com.w0rp.yotsubadroid;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.w0rp.androidutils.SLog;

import android.graphics.Color;
import android.text.Spanned;

public class Comment {
    public static final int QUOTE_COLOR = Color.rgb(0, 255, 0);

    private static class TagHandler extends DefaultHandler {
        List<Content> contentList;
        ContentType state = ContentType.PLAIN;
        Map<String, String> currentData;

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
                }
            } else if (name.equals("a")) {
                String aClass = atts.getValue("class");

                if ("quotelink".equals(aClass)) {
                    state = ContentType.QUOTELINK;

                    // TODO: Parse quotelink here, set result in currentData.
                    // 1234#5678 is for thread 1234, post 5678.
                    // If 1234 matches the thread later, we'll know
                    // that we have an inner-thread quote link.
                }
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

    public static enum ContentType {
        PLAIN, SPOILER, QUOTE, DEADLINK, QUOTELINK
    }

    public static class Content {
        private ContentType type;
        private String text;
        private Map<String, String> data;

        public Content(ContentType type, String text, Map<String, String> data) {
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

        public Map<String, String> getData() {
            if (this.data == null) {
                return Collections.emptyMap();
            }

            return this.data;
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

    private static String xmlify(String comment) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root>"
            + comment.replace("<br>", "<br />").replace("<wbr>", "")
            + "</root>";
    }

    private static List<Content> parse(String comment) {
        TagHandler handler = new TagHandler();

        try {
            System.setProperty("org.xml.sax.driver",
                "org.xmlpull.v1.sax2.Driver");
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(handler);

            // Pass in the XML to parse.
            reader.parse(new InputSource(new StringReader(xmlify(comment))));
        } catch (Exception e) {
            SLog.e(e);
        }

        return handler.contentList;
    }

    public static Spanned summaryText(String comment) {
        List<Content> contentList = parse(comment);
        SpanBuilder sb = new SpanBuilder();

        for (Content content : contentList) {
            String text = content.getText().replace("\n", " ");

            switch (content.getType()) {
            case PLAIN:
                sb.append(text);
            break;
            case QUOTE:
                sb.append(text, SpanBuilder.fg(QUOTE_COLOR));
            break;
            default:
            // The rest don't matter for summaries.
            break;
            }
        }

        return sb.span();
    }

    public static Spanned fullText(String comment) {
        List<Content> contentList = parse(comment);
        SpanBuilder sb = new SpanBuilder();

        for (Content content : contentList) {
            switch (content.getType()) {
            case PLAIN:
                sb.append(content.getText());
            break;
            case QUOTE:
                sb.append(content.getText(), SpanBuilder.fg(QUOTE_COLOR));
            break;
            case DEADLINK:
                sb.append(content.getText(), SpanBuilder.fg(QUOTE_COLOR),
                    SpanBuilder.strike());
            break;
            case QUOTELINK:
                // TODO: Add link behaviour here.
                sb.append(content.getText(), SpanBuilder.fg(QUOTE_COLOR));
            break;
            case SPOILER:
                // TODO: Add spoiler tapping behaviour here.
                sb.append(content.getText(), SpanBuilder.bg(0, 0, 0),
                    SpanBuilder.fg(255, 255, 255));
            break;
            }
        }

        return sb.span();
    }
}
