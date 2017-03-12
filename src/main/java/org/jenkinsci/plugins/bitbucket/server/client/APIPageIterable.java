package org.jenkinsci.plugins.bitbucket.server.client;

import org.jenkinsci.plugins.bitbucket.server.api.model.Page;
import org.jenkinsci.plugins.bitbucket.server.api.model.PageRequest;

import java.util.Iterator;

import static org.jenkinsci.plugins.bitbucket.server.api.model.PageRequest.nextPage;

/**
 * @author Robin Müller
 */
abstract class APIPageIterable<T> implements Iterable<T> {

    @Override
    public Iterator<T> iterator() {
        return new APIPageIterator();
    }

    protected abstract Page<T> getNextPage(PageRequest pageRequest);

    public class APIPageIterator implements Iterator<T> {

        private Page<T> currentPage;
        private Iterator<T> currentPageIterator;

        @Override
        public boolean hasNext() {
            if (currentPageIterator == null || (!currentPageIterator.hasNext() && !currentPage.getIsLastPage())) {
                currentPage = getNextPage(nextPage(currentPage));
                currentPageIterator = currentPage.getValues().iterator();
            }
            return currentPageIterator.hasNext();
        }

        @Override
        public T next() {
            return currentPageIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
