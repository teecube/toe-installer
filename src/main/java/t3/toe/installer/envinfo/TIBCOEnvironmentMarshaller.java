/**
 * (C) Copyright 2016-2018 teecube
 * (http://teecu.be) and others.
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
package t3.toe.installer.envinfo;

import com.tibco.envinfo.ObjectFactory;
import com.tibco.envinfo.TIBCOEnvironment;
import org.xml.sax.SAXException;
import t3.xml.XMLMarshall;

import javax.xml.bind.JAXBException;
import java.io.File;

public class TIBCOEnvironmentMarshaller extends XMLMarshall<TIBCOEnvironment, ObjectFactory> {

	public static final String NAMESPACE = "http://tibco.com/envInfo";

	public TIBCOEnvironmentMarshaller(File xmlFile) throws JAXBException, SAXException {
		super(xmlFile);
	}

}
