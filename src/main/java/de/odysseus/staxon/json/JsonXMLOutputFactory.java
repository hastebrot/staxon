/*
 * Copyright 2011 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odysseus.staxon.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import de.odysseus.staxon.event.SimpleXMLEventWriter;
import de.odysseus.staxon.json.stream.JsonStreamFactory;
import de.odysseus.staxon.json.stream.JsonStreamTarget;
import de.odysseus.staxon.json.stream.util.AutoArrayTarget;
import de.odysseus.staxon.json.stream.util.RemoveRootTarget;

public class JsonXMLOutputFactory extends XMLOutputFactory {
	/**
	 * <p>Start/end arrays automatically?</p>
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public static final String PROP_AUTO_ARRAY = "JsonXMLOutputFactory.autoArray";
	
	/**
	 * <p>Whether to use the {@link JsonXMLStreamConstants#MULTIPLE_PI_TARGET}
	 * processing instruction target to trigger an array start.
	 * If <code>true</code>, a PI is used to inform the writer to begin an array,
	 * passing the name of following multiple elements as data.
	 * The writer will close arrays automatically.</p>
	 *  
	 * <p>Note that the element given in the PI may be written zero times,
	 * indicating an empty array.</p>
	 * 
	 * <p>The default value is true.</p>
	 */
	public static final String PROP_MULTIPLE_PI = "JsonXMLOutputFactory.multiplePI";

	/**
	 * <p>JSON documents may have have multiple root properties. However,
	 * XML requires a single root element. This property takes the name
	 * of a "virtual" root element, which will be removed from the stream
	 * when writing.</p>
	 */
	public static final String PROP_VIRTUAL_ROOT = "JsonXMLOutputFactory.virtualRoot";

	/**
	 * Format output for better readability?
	 * 
	 * <p>The default value is <code>false</code>.</p>
	 */
	public static final String PROP_PRETTY_PRINT = "JsonXMLOutputFactory.prettyPrint";

	private JsonStreamFactory streamFactory = null;
	private boolean multiplePI = true;
	private String virtualRoot = null;
	private boolean autoArray = false;
	private boolean prettyPrint = false;

	public JsonXMLOutputFactory() throws FactoryConfigurationError {
		this(JsonStreamFactory.newFactory());
	}

	public JsonXMLOutputFactory(JsonStreamFactory streamFactory) {
		this.streamFactory = streamFactory;
	}
		
	private JsonStreamTarget decorate(JsonStreamTarget target) {
		if (autoArray) {
			target = new AutoArrayTarget(target);
		}
		if (virtualRoot != null) {
			target = new RemoveRootTarget(target, virtualRoot);
		}
		return target;
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
		try {
			return new JsonXMLStreamWriter(decorate(streamFactory.createJsonStreamTarget(stream, prettyPrint)), multiplePI);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding) throws XMLStreamException {
		try {
			return createXMLStreamWriter(new OutputStreamWriter(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public JsonXMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
		throw new UnsupportedOperationException();
	}

	@Override
	public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(result));
	}

	@Override
	public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(stream));
	}

	@Override
	public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(stream, encoding));
	}

	@Override
	public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException {
		return createXMLEventWriter(createXMLStreamWriter(stream));
	}

	public XMLEventWriter createXMLEventWriter(XMLStreamWriter writer) throws XMLStreamException {
		return new SimpleXMLEventWriter(writer);
	}

	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			if (Boolean.valueOf(value.toString())) {
				throw new IllegalArgumentException();
			}
		} else { // proprietary properties
			if (PROP_AUTO_ARRAY.equals(name)) {
				autoArray = Boolean.valueOf(value.toString());
			} else if (PROP_MULTIPLE_PI.equals(name)) {
				multiplePI = Boolean.valueOf(value.toString());;
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				virtualRoot = (String)value;
			} else if (PROP_PRETTY_PRINT.equals(name)) {
				prettyPrint = Boolean.valueOf(value.toString());
			} else {
				throw new IllegalArgumentException("Unsupported output property: " + name);
			}
		}
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			return false;
		} else { // proprietary properties
			 if (PROP_AUTO_ARRAY.equals(name)) {
				return autoArray;
			} else if (PROP_MULTIPLE_PI.equals(name)) {
				return multiplePI;
			} else if (PROP_VIRTUAL_ROOT.equals(name)) {
				return virtualRoot;
			} else if (PROP_PRETTY_PRINT.equals(name)) {
				return prettyPrint;
			} else {
				throw new IllegalArgumentException("Unsupported output property: " + name);
			}
		}
	}

	@Override
	public boolean isPropertySupported(String name) {
		if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
			return true;
		} else { // proprietary properties
			return Arrays.asList(PROP_AUTO_ARRAY, PROP_MULTIPLE_PI, PROP_VIRTUAL_ROOT, PROP_PRETTY_PRINT).contains(name);
		}
	}
}
