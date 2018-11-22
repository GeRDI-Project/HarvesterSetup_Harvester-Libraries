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
package de.gerdiproject.harvest.etls;

import de.gerdiproject.harvest.etls.extractors.${providerClassName}Extractor;
import de.gerdiproject.harvest.etls.extractors.${providerClassName}VO;
import de.gerdiproject.harvest.etls.transformers.${providerClassName}Transformer;
import de.gerdiproject.json.datacite.DataCiteJson;


/**
 * An ETL for harvesting ${providerName}.<br>
 * see ${providerUrl}.
 *
 * @author ${authorFullName}
 */
public class ${providerClassName}ETL extends StaticIteratorETL<${providerClassName}VO, DataCiteJson>
{
    /**
     * Constructor
     */
    public FaoStatETL()
    {
        super(new ${providerClassName}Extractor(), new ${providerClassName}Transformer());
    }
    
    // TODO 1. Check if StaticIteratorETL really suits your needs, or exchange it with any other AbstractETL.
    // TODO 2. Exchange ${providerClassName}VO with whatever is extracted from your DataProvider or populate it with fitting data.
    // TODO 3. Override registerParameters() if you need to register additional ETL parameters.
    // TODO 4. Override any other methods if needed.
}
