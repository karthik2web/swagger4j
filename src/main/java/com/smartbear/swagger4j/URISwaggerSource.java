/**
 *  Copyright 2013 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.swagger4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * SwaggerSource for reading Swagger definitions from a base URI. The extension of the URI will be used to
 * decide on the format (.json / .xml)
 */

public class URISwaggerSource implements SwaggerSource {

    private URI uri;

    public URISwaggerSource(URI uri) {
        this.uri = uri;
    }

    public static URI buildUri(URI uri, String path, Constants.Format format) {
        assert uri != null && path != null && format != null;

        String base = uri.toString();
        if (base.contains("?"))
            base = base.substring(0, base.indexOf('?'));

        path = path.replaceAll("\\{format\\}", format.getExtension());
        if (path.startsWith("/"))
        {
            base = base.substring(0, base.lastIndexOf('/'));


//            int ix = base.indexOf("://");
//            if( ix != -1 )
//            {
//                ix = base.indexOf( "/", ix+3 );
//                base = base.substring(0, ix );
//            }
        }

        return URI.create(base + path);
    }

    public static Constants.Format extractFormat(URI uri) {
        String path = uri.getPath() == null ? uri.toString() : uri.getPath().toLowerCase();
        Constants.Format format = Constants.Format.json;

        if (path.endsWith(".xml"))
            format = Constants.Format.xml;
        else if (path.endsWith(".json"))
            format = Constants.Format.json;
        else if (path.contains(".xml/"))
            format = Constants.Format.xml;

        return format;
    }

    @Override
    public Reader readResourceListing() throws IOException {
        return new InputStreamReader(uri.toURL().openStream());
    }

    @Override
    public Reader readApiDeclaration(String basePath, String path) throws IOException {

        try {
            path = path.replaceAll("\\{format\\}", getFormat().getExtension());

            if( !basePath.toLowerCase().startsWith("file:") && basePath.indexOf("://") == - 1)
            {
                String uriString = uri.toString();
                if( basePath.equals("."))
                    basePath = "";

                // find index to which the uriString should be used; depends on if the basePath is
                // absolute or relative.
                int ix = basePath.startsWith( "/" ) ?
                        uriString.indexOf("/", uriString.indexOf(":")+
                                (uriString.startsWith("file:") ? 1 : 4 )) :
                                        uriString.lastIndexOf("/");

                basePath = uriString.substring(0, ix) + basePath;
            }

            URI uri = new URI( basePath + path );

            return new InputStreamReader( uri.toURL().openStream());
        } catch (URISyntaxException e) {
            throw new IOException( e );
        }
    }

    @Override
    public Constants.Format getFormat() {
        return extractFormat(uri);
    }
}
