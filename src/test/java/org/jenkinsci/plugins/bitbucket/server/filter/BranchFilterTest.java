package org.jenkinsci.plugins.bitbucket.server.filter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Robin Müller
 */
public class BranchFilterTest {

    @Test
    public void isBranchAllowed_noExcludes() {
        BranchFilter filter = new BranchFilter("*", null);

        assertTrue(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_containingSlash() {
        BranchFilter filter = new BranchFilter("*", null);

        assertTrue(filter.isBranchAllowed("feature/cross-domain-usage"));
    }

    @Test
    public void isBranchAllowed_withExcludes_noMatch() {
        BranchFilter filter = new BranchFilter("*", "excluded");

        assertTrue(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withMultipleExcludes_noMatch() {
        BranchFilter filter = new BranchFilter("*", "excluded-1, excluded-2");

        assertTrue(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withExcludes_withMatch() {
        BranchFilter filter = new BranchFilter("*", "excluded");

        assertFalse(filter.isBranchAllowed("excluded"));
    }

    @Test
    public void isBranchAllowed_withMultipleExcludes_withMatch() {
        BranchFilter filter = new BranchFilter("*", "excluded-1, excluded-2");

        assertFalse(filter.isBranchAllowed("excluded-1"));
        assertFalse(filter.isBranchAllowed("excluded-2"));
    }

    @Test
    public void isBranchAllowed_withWildCardExcludes_noMatch() {
        BranchFilter filter = new BranchFilter("*", "excluded*");

        assertTrue(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withWildCardExcludes_withMatch() {
        BranchFilter filter = new BranchFilter("*", "excluded*");

        assertFalse(filter.isBranchAllowed("excluded-1"));
        assertFalse(filter.isBranchAllowed("excluded-2"));
    }

    @Test
    public void isBranchAllowed_withIncludes_noMatch() {
        BranchFilter filter = new BranchFilter("included", "");

        assertFalse(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withMultipleIncludes_noMatch() {
        BranchFilter filter = new BranchFilter("included-1, included-2", "");

        assertFalse(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withIncludes_withMatch() {
        BranchFilter filter = new BranchFilter("included", "");

        assertTrue(filter.isBranchAllowed("included"));
    }

    @Test
    public void isBranchAllowed_withMultipleIncludes_withMatch() {
        BranchFilter filter = new BranchFilter("included-1, included-2", "");

        assertTrue(filter.isBranchAllowed("included-1"));
        assertTrue(filter.isBranchAllowed("included-2"));
    }

    @Test
    public void isBranchAllowed_withWildCardIncludes_noMatch() {
        BranchFilter filter = new BranchFilter("included*", "");

        assertFalse(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withWildCardIncludes_withMatch() {
        BranchFilter filter = new BranchFilter("included*", "");

        assertTrue(filter.isBranchAllowed("included-1"));
        assertTrue(filter.isBranchAllowed("included-2"));
    }

    @Test
    public void isBranchAllowed_withIncludesAndExcludes_noMatch() {
        BranchFilter filter = new BranchFilter("included", "excluded");

        assertFalse(filter.isBranchAllowed("test"));
    }

    @Test
    public void isBranchAllowed_withIncludesAndExcludes_includeMatch() {
        BranchFilter filter = new BranchFilter("included", "excluded");

        assertTrue(filter.isBranchAllowed("included"));
    }

    @Test
    public void isBranchAllowed_withIncludesAndExcludes_excludeMatch() {
        BranchFilter filter = new BranchFilter("included", "excluded");

        assertFalse(filter.isBranchAllowed("excluded"));
    }
}
