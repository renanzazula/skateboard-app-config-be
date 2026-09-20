package com.skateboard.appconfig.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void generatesAnIdWhenNoneIsForwarded() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.HEADER)).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        ArgumentCaptor<String> header = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(org.mockito.ArgumentMatchers.eq(CorrelationIdFilter.HEADER), header.capture());
        assertThat(header.getValue()).isNotBlank();
        verify(chain).doFilter(request, response);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void reusesAForwardedCorrelationId() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.HEADER)).thenReturn("existing-id");

        filter.doFilterInternal(request, response, chain);

        verify(response).setHeader(CorrelationIdFilter.HEADER, "existing-id");
        verify(chain).doFilter(request, response);
    }

    @Test
    void removesTheMdcEntryEvenWhenTheChainThrows() throws Exception {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.HEADER)).thenReturn("existing-id");
        Mockito.doThrow(new RuntimeException("boom")).when(chain).doFilter(any(), any());

        try {
            filter.doFilterInternal(request, response, chain);
        } catch (RuntimeException expected) {
            // expected — the filter re-throws after cleaning up MDC
        }

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
