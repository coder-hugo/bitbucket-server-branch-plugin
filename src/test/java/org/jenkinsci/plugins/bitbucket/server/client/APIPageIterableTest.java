package org.jenkinsci.plugins.bitbucket.server.client;

import org.jenkinsci.plugins.bitbucket.server.api.model.Page;
import org.jenkinsci.plugins.bitbucket.server.api.model.PageRequest;
import org.jenkinsci.plugins.bitbucket.server.api.model.builder.PageBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @author Robin Müller
 */
public class APIPageIterableTest {

    @Test
    public void iterateOnePage() {
        APIPageIterable<String> objects = new APIPageIterable<String>() {
            @Override
            protected Page<String> getNextPage(PageRequest pageRequest) {
                return PageBuilder.<String>aPage()
                        .isLastPage(true)
                        .values(Arrays.asList("1st", "2nd", "3rd"))
                        .build();
            }
        };

        assertThat(objects, containsInAnyOrder("1st", "2nd", "3rd"));
    }

    @Test
    public void iterateMultiplePages() {
        APIPageIterable<String> objects = new APIPageIterable<String>() {
            @Override
            protected Page<String> getNextPage(PageRequest pageRequest) {
                boolean isLastPage = pageRequest != null && pageRequest.getStart() == 5;
                int start = pageRequest == null ? 0 : pageRequest.getStart();
                return PageBuilder.<String>aPage()
                        .start(start)
                        .limit(1)
                        .isLastPage(isLastPage)
                        .values(Collections.singletonList(String.valueOf(start)))
                        .build();
            }
        };

        assertThat(objects, containsInAnyOrder("0", "1", "2", "3", "4", "5"));
    }
}
