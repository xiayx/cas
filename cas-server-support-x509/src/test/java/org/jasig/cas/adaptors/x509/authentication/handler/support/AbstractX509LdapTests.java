package org.jasig.cas.adaptors.x509.authentication.handler.support;

import com.unboundid.ldap.sdk.LDAPException;
import org.apache.commons.io.IOUtils;
import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.LdapTestUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collection;

/**
 * Parent class to help with testing x509 operations that deal with LDAP.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractX509LdapTests extends AbstractLdapTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractX509LdapTests.class);

    private static final String DN = "CN=x509,ou=people,dc=example,dc=org";

    public static void bootstrap() throws Exception {
        try {
            initDirectoryServer();
            getDirectory().populateEntries(new ClassPathResource("ldif/users-x509.ldif").getInputStream());
            populateCertificateRevocationListAttribute();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void populateCertificateRevocationListAttribute() throws IOException, LDAPException, LdapException {
        /**
         * Dynamically set the attribute value to the crl content.
         * Encode it as base64 first. Doing this in the code rather
         * than in the ldif file to ensure the attribute can be populated
         * without dependencies on the classpath and or filesystem.
         */
        final Collection<LdapEntry> col = getDirectory().getLdapEntries();
        for (final LdapEntry ldapEntry : col) {
            if (ldapEntry.getDn().equals(DN)) {
                final LdapAttribute attr = new LdapAttribute(true);

                byte[] value = new byte[1024];
                IOUtils.read(new ClassPathResource("userCA-valid.crl").getInputStream(), value);
                value = CompressionUtils.encodeBase64ToByteArray(value);
                attr.setName("certificateRevocationList");
                attr.addBinaryValue(value);
                LdapTestUtils.modifyLdapEntry(getDirectory().getConnection(), ldapEntry, attr);

            }
        }
    }

    public final String getTestDN() {
        return DN;
    }
}
