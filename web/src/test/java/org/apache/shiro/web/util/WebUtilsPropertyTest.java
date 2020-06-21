package org.apache.shiro.web.util;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;

public class WebUtilsPropertyTest {

    @Property
    boolean path_within_application_is_within(@ForAll("urlPath") String path) throws UnsupportedEncodingException {
        final HttpServletRequest request = niceMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("/app").times(2);
        expect(request.getServletPath()).andReturn("").atLeastOnce();
        expect(request.getPathInfo()).andReturn(path).atLeastOnce();
        replay(request);


        // when
        final String pathWithinApplication = WebUtils.getPathWithinApplication(request);
        final String decodedPath = URLDecoder.decode(path, "ISO-8859-1");

        final boolean equals = decodedPath.equals(pathWithinApplication);
        if (!equals) {
            System.out.println("Path: [" + path + "] = [" + decodedPath + "]. In app: [" + pathWithinApplication + "].");
        }
        return equals;
    }

    // fuzzy property-based test to see if a filter gets chained
    @Provide
    Arbitrary<String> urlPath() {
        return Arbitraries.strings()
                .withChars("abzABZ./%C1234567890CDEFcdef\\")
                //.all()
                .ofMinLength(10)
                .ofMaxLength(36)
                .filter(str -> !str.startsWith("/"))
                .filter(str -> !str.contains("//"))
                .filter(this::canBeUrlDecoded)
                //.filter(str -> !str.startsWith("/."))
                //.filter(str -> !str.startsWith(".."))
                .map(str -> "/resource/" + str)
                ;
    }

    private boolean canBeUrlDecoded(String str) {
        try {
            URLDecoder.decode(str, "ISO-8859-1");
            return true;
        } catch (UnsupportedEncodingException encodingException) {
            return false;
        }
    }

}
