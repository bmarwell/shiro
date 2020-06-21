package org.apache.shiro.web.filter.mgt;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class PathMatchingFilterChainResolverPropertyTest {

    @Property
    boolean chain_must_not_be_null(@ForAll("urlPath") String path) {
        // given request with path
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("app");
        expect(request.getServletPath()).andReturn(path);
        expect(request.getPathInfo()).andReturn(null);
        replay(request);
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        FilterChain originalChain = createNiceMock(FilterChain.class);

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.getFilterChainManager().addToChain("/**", "authcBasic");

        // when
        final FilterChain chain = resolver.getChain(request, response, originalChain);

        // then
        return chain != null;
    }

    @Property
    boolean chain_must_not_be_null_for_path(@ForAll("urlPath") String path) {
        // given request with path
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("app");
        expect(request.getServletPath()).andReturn("");
        expect(request.getPathInfo()).andReturn(path);
        replay(request);
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        FilterChain originalChain = createNiceMock(FilterChain.class);

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.getFilterChainManager().addToChain("/resource/**", "authcBasic");

        // when
        final FilterChain chain = resolver.getChain(request, response, originalChain);

        // then
        return chain != null;
    }


    // fuzzy property-based test to see if a filter gets chained
    @Provide
    Arbitrary<String> urlPath() {
        return Arbitraries.strings()
                .all()
                .ofMinLength(2)
                .ofMaxLength(2048)
                //.filter(str -> !str.startsWith("/"))
                //.filter(str -> !str.startsWith("/."))
                //.filter(str -> !str.startsWith(".."))
                .map(str -> "/resource/" + str)
                ;
    }

}
