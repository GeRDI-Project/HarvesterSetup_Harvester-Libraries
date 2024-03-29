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
package de.gerdiproject.harvest.etls.transformers;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.etls.extractors.${providerClassName}VO;
import de.gerdiproject.json.datacite.DataCiteJson;

/**
 * This transformer parses metadata from a {@linkplain ${providerClassName}VO}
 * and creates {@linkplain DataCiteJson} objects from it.
 *
 * @author ${authorFullName}
 */
public class ${providerClassName}Transformer extends AbstractIteratorTransformer<${providerClassName}VO, DataCiteJson>
{
    @Override
    public void init(final AbstractETL<?, ?> etl)
    {
        // TODO retrieve parameter values from the ETL, if needed
    }


    @Override
    protected DataCiteJson transformElement(final ${providerClassName}VO source)
    {
        // create the document
        final DataCiteJson document = new DataCiteJson(createIdentifier(source));

        // TODO add all possible metadata to the document

        return document;
    }

    
    /**
     * Creates a unique identifier for a document from ${providerClassName}.
     *
     * @param source the source object that contains all metadata that is needed
     *
     * @return a unique identifier of this document
     */
    private String createIdentifier(final ${providerClassName}VO source)
    {
        // TODO retrieve a unique identifier from the source and remove exception
		throw new UnsupportedOperationException();
    }


    @Override
    public void clear()
    {
        // TODO close any open streams, if there are none, comment with "// nothing to clean up"        
    }
}
