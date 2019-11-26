/**
 * Copyright © ${creationYear} ${authorFullName} (http://www.gerdi-project.de)
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
package de.gerdiproject.harvest.etls.extractors;

import java.io.File;
import java.nio.charset.StandardCharsets;

import de.gerdiproject.harvest.${providerClassName}ContextListener;
import de.gerdiproject.harvest.application.ContextListener;
import de.gerdiproject.harvest.etls.AbstractIteratorETL;
import de.gerdiproject.harvest.etls.${providerClassName}ETL;
import de.gerdiproject.harvest.utils.data.DiskIO;
import de.gerdiproject.json.GsonUtils;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This class provides Unit Tests for the {@linkplain ${providerClassName}Extractor}.
 *
 * @author ${authorFullName}
 */
public class ${providerClassName}ExtractorTest extends AbstractIteratorExtractorTest<${providerClassName}VO>
{
    final DiskIO diskReader = new DiskIO(GsonUtils.createGerdiDocumentGsonBuilder().create(), StandardCharsets.UTF_8);

	// TODO if the extractor does not generate ${providerClassName}VOs, replace all occurrences in the unit tests accordingly

    @Override
    protected ContextListener getContextListener()
    {
        return new ${providerClassName}ContextListener();
    }


    @Override
    protected AbstractIteratorETL<${providerClassName}VO, DataCiteJson> getEtl()
    {
        return new ${providerClassName}ETL();
    }

    
    @Override
    protected File getConfigFile()
    {
		// TODO check out the src\test\resources\de\gerdiproject\harvest\etls\extractors\${providerClassName}ExtractorTest\config.json
		// TODO to define all parameters required for testing
        return getResource("config.json");
    }
    

    @Override
    protected File getMockedHttpResponseFolder()
    {
		// TODO check out the src\test\resources\de\gerdiproject\harvest\etls\extractors\${providerClassName}ExtractorTest\mockedHttpRequests folder
		// TODO and add mocked HTTP responses to be able to test regardless of the internet connection
        return getResource("mockedHttpResponses");
    }


    @Override
    protected ${providerClassName}VO getExpectedOutput()
    {
		// TODO add the src\test\resources\de\gerdiproject\harvest\etls\extractors\${providerClassName}ExtractorTest\output.json
		// TODO if the extracted object is not json, adjust the file name accordingly
        final File resource = getResource("output.json");
        return diskReader.getObject(resource, ${providerClassName}VO.class);
    }
}
