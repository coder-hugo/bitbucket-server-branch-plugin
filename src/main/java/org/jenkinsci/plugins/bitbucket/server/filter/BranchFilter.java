package org.jenkinsci.plugins.bitbucket.server.filter;

import com.google.common.base.Converter;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;
import org.springframework.util.AntPathMatcher;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * @author Robin Müller
 */
public class BranchFilter {

    private final Iterable<Pattern> includes;
    private final Iterable<Pattern> excludes;

    @BitbucketPojoBuilder
    public BranchFilter(String includes, String excludes) {
        this.includes = convert(includes);
        this.excludes = convert(excludes == null ? "" : excludes);
    }

    public boolean isBranchAllowed(String branch) {
        return isBranchNotExcluded(branch) && isBranchIncluded(branch);
    }

    private boolean isBranchNotExcluded(String branch) {
        for (Pattern exclude : excludes) {
            if (exclude.matcher(branch).matches()) {
                return false;
            }
        }
        return true;
    }

    private boolean isBranchIncluded(String branch) {
        for (Pattern include : includes) {
            if (include.matcher(branch).matches()) {
                return true;
            }
        }
        return !includes.iterator().hasNext();
    }

    private Iterable<Pattern> convert(String commaSeparatedString) {
        return FluentIterable.from(Splitter.on(",").omitEmptyStrings().trimResults().split(commaSeparatedString)).transform(new Function<String, Pattern>() {
            @Nullable
            @Override
            public Pattern apply(@Nullable String input) {
                return input == null ? null : Pattern.compile(input.replace("?", ".?").replace("*", ".*?"));
            }
        });
    }
}
