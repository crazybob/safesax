/**
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package safesax;

import junit.framework.TestCase;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

public class SafeSaxTest extends TestCase {

  protected static final String ATOM_NAMESPACE
      = "http://www.w3.org/2005/Atom";

  public static void parse(String xml, ContentHandler contentHandler)
      throws SAXException {
    try {
      Parsers.parse(new StringReader(xml), contentHandler);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void testListeners() throws SAXException {
    String xml = "<feed xmlns='http://www.w3.org/2005/Atom'>\n"
        + "<entry>\n"
        + "<id>a</id>\n"
        + "</entry>\n"
        + "<entry>\n"
        + "<id>b</id>\n"
        + "</entry>\n"
        + "</feed>\n";

    RootElement root = new RootElement(ATOM_NAMESPACE, "feed");
    Element entry = root.requireChild(ATOM_NAMESPACE, "entry");
    Element id = entry.requireChild(ATOM_NAMESPACE, "id");

    SafeSaxTest.ElementCounter rootCounter
        = new SafeSaxTest.ElementCounter();
    SafeSaxTest.ElementCounter entryCounter
        = new SafeSaxTest.ElementCounter();
    SafeSaxTest.TextElementCounter idCounter
        = new SafeSaxTest.TextElementCounter();

    root.setElementListener(rootCounter);
    entry.setElementListener(entryCounter);
    id.setTextElementListener(idCounter);

    parse(xml, root.getContentHandler());

    assertEquals(1, rootCounter.starts);
    assertEquals(1, rootCounter.ends);
    assertEquals(2, entryCounter.starts);
    assertEquals(2, entryCounter.ends);
    assertEquals(2, idCounter.starts);
    assertEquals("ab", idCounter.bodies);
  }

  static class ElementCounter implements ElementListener {

    int starts = 0;
    int ends = 0;

    public void start(Attributes attributes) {
      starts++;
    }

    public void end() {
      ends++;
    }
  }

  static class TextElementCounter implements TextElementListener {

    int starts = 0;
    String bodies = "";

    public void start(Attributes attributes) {
      starts++;
    }

    public void end(String body) {
      this.bodies += body;
    }
  }

  public void testMissingChild() {
    String xml = "<feed></feed>";

    RootElement root = new RootElement("feed");
    root.requireChild("entry");

    try {
      parse(xml, root.getContentHandler());
      fail();
    } catch (SAXException e) { /* expected */ }
  }

  public void testMixedContent() {
    String xml = "<feed><entry></entry></feed>";

    RootElement root = new RootElement("feed");
    root.setEndTextElementListener(new EndTextElementListener() {
      public void end(String body) {
      }
    });

    try {
      parse(xml, root.getContentHandler());
      fail();
    } catch (SAXException e) { /* expected */ }
  }
}
