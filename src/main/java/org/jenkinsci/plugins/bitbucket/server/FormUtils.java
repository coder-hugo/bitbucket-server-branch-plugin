package org.jenkinsci.plugins.bitbucket.server;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Util;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMSourceOwner;

import java.net.MalformedURLException;
import java.net.URL;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf;

/**
 * Utility class for common code accessing credentials
 */
final class FormUtils {

    static final String SAME = "SAME";

    private FormUtils() { }

    static ListBoxModel fillCheckoutCredentials(String serverUrl, SCMSourceOwner context) {
        StandardListBoxModel result = new StandardListBoxModel();
        result.add(Messages.Credentials_SAME(), SAME);
        result.add(Messages.Credentials_ANONYMOUS(), null);
        return result.includeMatchingAs(context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
                                        context,
                                        StandardCredentials.class,
                                        URIRequirementBuilder.fromUri(serverUrl).build(),
                                        anyOf(instanceOf(StandardCredentials.class)));
    }

    static ListBoxModel fillScanCredentials(String serverUrl, SCMSourceOwner context) {
        StandardListBoxModel result = new StandardListBoxModel();
        result.add(Messages.Credentials_ANONYMOUS(), null);
        return result.includeMatchingAs(context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
                                        context,
                                        StandardUsernameCredentials.class,
                                        URIRequirementBuilder.fromUri(serverUrl).build(),
                                        anyOf(instanceOf(StandardUsernamePasswordCredentials.class)));
    }

    static FormValidation checkScanCredentialsId(String value) {
        if (Util.fixEmpty(value) == null) {
            return FormValidation.warning(Messages.Warning_ScanCredendials());
        } else {
            return FormValidation.ok();
        }
    }

    static FormValidation checkBitbucketServerUrl(String value) {
        String url = Util.fixEmpty(value);
        if (url == null) {
            return FormValidation.error(Messages.Error_ServerUrlRequired());
        }
        try {
            new URL(value);
        } catch (MalformedURLException e) {
            return FormValidation.error(Messages.Error_InvalidUrl(e.getMessage()));
        }
        return FormValidation.ok();
    }

    static FormValidation checkRequiredField(String value, String errorMessage) {
        if (Util.fixEmpty(value) == null) {
            return FormValidation.error(errorMessage);
        }
        return FormValidation.ok();
    }
}
