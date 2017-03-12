package org.jenkinsci.plugins.bitbucket.server.filter;

import com.google.common.base.Splitter;
import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;
import org.springframework.util.AntPathMatcher;

/**
 * @author Robin Müller
 */
public class BranchFilter {

    private final Iterable<String> includes;
    private final Iterable<String> excludes;

    @BitbucketPojoBuilder
    public BranchFilter(String includes, String excludes) {
        this.includes = convert(includes);
        this.excludes = convert(excludes == null ? "" : excludes);
    }

    public boolean isBranchAllowed(String branch) {
        return isBranchNotExcluded(branch) && isBranchIncluded(branch);
    }

    private boolean isBranchNotExcluded(String branch) {
        AntPathMatcher matcher = new AntPathMatcher();
        for (String exclude : excludes) {
            if (matcher.match(exclude, branch)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBranchIncluded(String branch) {
        AntPathMatcher matcher = new AntPathMatcher();
        for (String include : includes) {
            if (matcher.match(include, branch)) {
                return true;
            }
        }
        return !includes.iterator().hasNext();
    }

    private Iterable<String> convert(String commaSeparatedString) {
        return Splitter.on(",").omitEmptyStrings().trimResults().split(commaSeparatedString);
    }
}
