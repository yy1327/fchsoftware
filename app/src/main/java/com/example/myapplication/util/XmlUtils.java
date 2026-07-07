package com.example.myapplication.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

public class XmlUtils {

    public static Element parseXml(String xml) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(xml));
            return document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getElementText(Element parent, String tagName) {
        Element element = parent.element(tagName);
        if (element != null) {
            return element.getTextTrim();
        }
        return null;
    }

    public static String createKeepaliveXml(int sn, String deviceId) {
        return "<?xml version=\"1.0\"?>" +
                "<Notify>" +
                "<CmdType>Keepalive</CmdType>" +
                "<SN>" + sn + "</SN>" +
                "<DeviceID>" + deviceId + "</DeviceID>" +
                "<Status>OK</Status>" +
                "</Notify>";
    }
}