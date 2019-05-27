/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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
package t3.toe.installer.environments;

import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXException;
import t3.xml.XMLMarshall;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStream;

public class EnvironmentsMarshaller extends XMLMarshall<Environments, ObjectFactory> {

	public static final String NAMESPACE = "http://teecu.be/toe-installer/environments";

	public EnvironmentsMarshaller(File xmlFile, InputStream xsdStream) throws JAXBException, SAXException {
		super(xmlFile, xsdStream);
	}

	public EnvironmentsMarshaller(File xmlFile) throws JAXBException, SAXException {
		super(xmlFile);
	}

	public static EnvironmentsMarshaller getEnvironmentMarshaller(File environmentsTopology) throws MojoExecutionException {
		EnvironmentsMarshaller environmentsMarshaller = null;
		try {
			String filename = "/xsd/environments.xsd";
			InputStream configStream = EnvironmentInstallerMojo.class.getResourceAsStream(filename);

			environmentsMarshaller = new EnvironmentsMarshaller(environmentsTopology, configStream);
		} catch (JAXBException | SAXException e) {
			throw new MojoExecutionException("Unable to load topology from file '" + environmentsTopology.getAbsolutePath() + "'", e);
		}
		if (environmentsMarshaller == null) {
			throw new MojoExecutionException("Unable to load topology from file '" + environmentsTopology.getAbsolutePath() + "'");
		}
		return environmentsMarshaller;
	}
}
