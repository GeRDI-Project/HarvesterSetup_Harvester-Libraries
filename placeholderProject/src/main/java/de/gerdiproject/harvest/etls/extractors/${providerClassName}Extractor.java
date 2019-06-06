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

import java.util.Iterator;

import de.gerdiproject.harvest.etls.AbstractETL;
import de.gerdiproject.harvest.utils.data.HttpRequester;

/**
 * This {@linkplain AbstractIteratorExtractor} implementation extracts all
 * (meta-)data from ${providerName} and bundles it into a {@linkplain ${providerClassName}VO}.
 *
 * @author ${authorFullName}
 */
public class ${providerClassName}Extractor extends AbstractIteratorExtractor<${providerClassName}VO>
{
    // protected fields that may be used by the inner iterator class
    protected final HttpRequester httpRequester;

    private String version = null;
    private int sourceDocumentCount = -1;


    /**
     * Simple constructor.
     */
    public ${providerClassName}Extractor()
    {
        this.httpRequester = new HttpRequester();
    }


    @Override
    public void init(AbstractETL<?, ?> etl)
    {
        super.init(etl);

        this.httpRequester.setCharset(etl.getCharset());

        // TODO if possible, extract some metadata in order to determine the size and a version string
        // final ${providerClassName}ETL specificEtl = (${providerClassName}ETL) etl;
        // this.version = ;
        // this.sourceDocumentCount = ;
    }


    @Override
    public String getUniqueVersionString()
    {
        return version;
    }



    @Override
    public int size()
    {
        return sourceDocumentCount;
    }


    @Override
    protected Iterator<${providerClassName}VO> extractAll() throws ExtractorException
    {
        return new ${providerClassName}Iterator();
    }


    @Override
    public void clear()
    {
        // TODO close any open streams, if there are none, comment with "// nothing to clean up"
    }


    /**
     * TODO add a description here
     *
     * @author ${authorFullName}
     */
    private class ${providerClassName}Iterator implements Iterator<${providerClassName}VO>
    {
        @Override
        public boolean hasNext()
        {
            // TODO implement and remove exception
			throw new UnsupportedOperationException();
        }


        @Override
        public ${providerClassName}VO next()
        {
            // TODO implement and remove exception
			throw new UnsupportedOperationException();
        }
    }
}
