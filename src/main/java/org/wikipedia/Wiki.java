/**
 *  @(#)Wiki.java 0.31 29/08/2015
 *  Copyright (C) 2007 - 2014 MER-C and contributors
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version. Additionally
 *  this file is subject to the "Classpath" exception.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.wikipedia;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.*;
import java.util.zip.GZIPInputStream;

import javax.security.auth.login.*;

/**
 *  This is a somewhat sketchy bot framework for editing MediaWiki wikis.
 *  Requires JDK 1.8 or greater. Uses the <a
 *  href="//www.mediawiki.org/wiki/API:Main_page">MediaWiki API</a> for most
 *  operations. It is recommended that the server runs the latest version
 *  of MediaWiki (1.23), otherwise some functions may not work.
 *  <p>
 *  Extended documentation is available
 *  <a href="//github.com/MER-C/wiki-java/wiki/Extended-documentation">here</a>.
 *  All wikilinks are relative to the English Wikipedia and all timestamps are in
 *  your wiki's time zone.
 *  </p>
 *  Please file bug reports <a href="//en.wikipedia.org/wiki/User_talk:MER-C">here</a>
 *  or at the <a href="//github.com/MER-C/wiki-java/issues">Github issue tracker</a>.
 *
 *  @author MER-C and contributors
 *  @version 0.31
 */
public class Wiki implements Serializable
{
    // Master TODO list:
    // *Admin stuff
    // *More multiqueries
    // *Generators (hard)

    // NAMESPACES

    /**
     *  Denotes the namespace of images and media, such that there is no
     *  description page. Uses the "Media:" prefix.
     *  @see #FILE_NAMESPACE
     *  @since 0.03
     */
    public static final int MEDIA_NAMESPACE = -2;

    /**
     *  Denotes the namespace of pages with the "Special:" prefix. Note
     *  that many methods dealing with special pages may spew due to
     *  raw content not being available.
     *  @since 0.03
     */
    public static final int SPECIAL_NAMESPACE = -1;

    /**
     *  Denotes the main namespace, with no prefix.
     *  @since 0.03
     */
    public static final int MAIN_NAMESPACE = 0;

    /**
     *  Denotes the namespace for talk pages relating to the main
     *  namespace, denoted by the prefix "Talk:".
     *  @since 0.03
     */
    public static final int TALK_NAMESPACE = 1;

    /**
     *  Denotes the namespace for user pages, given the prefix "User:".
     *  @since 0.03
     */
    public static final int USER_NAMESPACE = 2;

    /**
     *  Denotes the namespace for user talk pages, given the prefix
     *  "User talk:".
     *  @since 0.03
     */
    public static final int USER_TALK_NAMESPACE = 3;

    /**
     *  Denotes the namespace for pages relating to the project,
     *  with prefix "Project:". It also goes by the name of whatever
     *  the project name was.
     *  @since 0.03
     */
    public static final int PROJECT_NAMESPACE = 4;

    /**
     *  Denotes the namespace for talk pages relating to project
     *  pages, with prefix "Project talk:". It also goes by the name
     *  of whatever the project name was, + "talk:".
     *  @since 0.03
     */
    public static final int PROJECT_TALK_NAMESPACE = 5;

    /**
     *  Denotes the namespace for file description pages. Has the prefix
     *  "File:". Do not create these directly, use upload() instead.
     *  @see #MEDIA_NAMESPACE
     *  @since 0.25
     */
    public static final int FILE_NAMESPACE = 6;

    /**
     *  Denotes talk pages for file description pages. Has the prefix
     *  "File talk:".
     *  @since 0.25
     */
    public static final int FILE_TALK_NAMESPACE = 7;

    /**
     *  Denotes the namespace for (wiki) system messages, given the prefix
     *  "MediaWiki:".
     *  @since 0.03
     */
    public static final int MEDIAWIKI_NAMESPACE = 8;

    /**
     *  Denotes the namespace for talk pages relating to system messages,
     *  given the prefix "MediaWiki talk:".
     *  @since 0.03
     */
    public static final int MEDIAWIKI_TALK_NAMESPACE = 9;

    /**
     *  Denotes the namespace for templates, given the prefix "Template:".
     *  @since 0.03
     */
    public static final int TEMPLATE_NAMESPACE = 10;

    /**
     *  Denotes the namespace for talk pages regarding templates, given
     *  the prefix "Template talk:".
     *  @since 0.03
     */
    public static final int TEMPLATE_TALK_NAMESPACE = 11;

    /**
     *  Denotes the namespace for help pages, given the prefix "Help:".
     *  @since 0.03
     */
    public static final int HELP_NAMESPACE = 12;

    /**
     *  Denotes the namespace for talk pages regarding help pages, given
     *  the prefix "Help talk:".
     *  @since 0.03
     */
    public static final int HELP_TALK_NAMESPACE = 13;

    /**
     *  Denotes the namespace for category description pages. Has the
     *  prefix "Category:".
     *  @since 0.03
     */
    public static final int CATEGORY_NAMESPACE = 14;

    /**
     *  Denotes the namespace for talk pages regarding categories. Has the
     *  prefix "Category talk:".
     *  @since 0.03
     */
    public static final int CATEGORY_TALK_NAMESPACE = 15;

    /**
     *  Denotes all namespaces.
     *  @since 0.03
     */
    public static final int ALL_NAMESPACES = 0x09f91102;

    // LOG TYPES

    /**
     *  Denotes all logs.
     *  @since 0.06
     */
    public static final String ALL_LOGS = "";

    /**
     *  Denotes the user creation log.
     *  @since 0.06
     */
    public static final String USER_CREATION_LOG = "newusers";

    /**
     *  Denotes the upload log.
     *  @since 0.06
     */
    public static final String UPLOAD_LOG = "upload";

    /**
     *  Denotes the deletion log.
     *  @since 0.06
     */
    public static final String DELETION_LOG = "delete";

    /**
     *  Denotes the move log.
     *  @since 0.06
     */
    public static final String MOVE_LOG = "move";

    /**
     *  Denotes the block log.
     *  @since 0.06
     */
    public static final String BLOCK_LOG = "block";

    /**
     *  Denotes the protection log.
     *  @since 0.06
     */
    public static final String PROTECTION_LOG = "protect";

    /**
     *  Denotes the user rights log.
     *  @since 0.06
     */
    public static final String USER_RIGHTS_LOG = "rights";

    /**
     *  Denotes the user renaming log.
     *  @since 0.06
     */
    public static final String USER_RENAME_LOG = "renameuser";

    /**
     *  Denotes the page importation log.
     *  @since 0.08
     */
    public static final String IMPORT_LOG = "import";

    /**
     *  Denotes the edit patrol log.
     *  @since 0.08
     */
    public static final String PATROL_LOG = "patrol";

    // PROTECTION LEVELS

    /**
     *  Denotes a non-protected page.
     *  @since 0.09
     */
    public static final String NO_PROTECTION = "all";

    /**
     *  Denotes semi-protection (i.e. only autoconfirmed users can perform a
     *  particular action).
     *  @since 0.09
     */
    public static final String SEMI_PROTECTION = "autoconfirmed";

    /**
     *  Denotes full protection (i.e. only admins can perfom a particular action).
     *  @since 0.09
     */
    public static final String FULL_PROTECTION = "sysop";

    // ASSERTION MODES

    /**
     *  Use no assertions (i.e. 0).
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_NONE = 0;

    /**
     *  Assert that we are logged in (i.e. 1). This is checked every action.
     *  @see #setAssertionMode
     *  @since 0.30
     */
    public static final int ASSERT_USER = 1;

    /**
     *  Assert that we have a bot flag (i.e. 2). This is checked every action.
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_BOT = 2;

    /**
     *  Assert that we have no new messages. Not defined officially, but
     *  some bots have this. This is checked intermittently.
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public static final int ASSERT_NO_MESSAGES = 4;

    /**
     *  Assert that we have a sysop flag (i.e. 8). This is checked intermittently.
     *  @see #setAssertionMode
     *  @since 0.30
     */
    public static final int ASSERT_SYSOP = 8;

    // RC OPTIONS

    /**
     *  In queries against the recent changes table, this would mean we don't
     *  fetch anonymous edits.
     *  @since 0.20
     */
    public static final int HIDE_ANON = 1;

    /**
     *  In queries against the recent changes table, this would mean we don't
     *  fetch edits made by bots.
     *  @since 0.20
     */
    public static final int HIDE_BOT = 2;

    /**
     *  In queries against the recent changes table, this would mean we don't
     *  fetch by the logged in user.
     *  @since 0.20
     */
    public static final int HIDE_SELF = 4;

    /**
     *  In queries against the recent changes table, this would mean we don't
     *  fetch minor edits.
     *  @since 0.20
     */
    public static final int HIDE_MINOR = 8;

    /**
     *  In queries against the recent changes table, this would mean we don't
     *  fetch patrolled edits.
     *  @since 0.20
     */
    public static final int HIDE_PATROLLED = 16;

    // REVISION OPTIONS

    /**
     *  In <tt>Revision.diff()</tt>, denotes the next revision.
     *  @see org.wikipedia.Wiki.Revision#diff(org.wikipedia.Wiki.Revision)
     *  @since 0.21
     */
    public static final long NEXT_REVISION = -1L;

    /**
     *  In <tt>Revision.diff()</tt>, denotes the current revision.
     *  @see org.wikipedia.Wiki.Revision#diff(org.wikipedia.Wiki.Revision)
     *  @since 0.21
     */
    public static final long CURRENT_REVISION = -2L;

    /**
     *  In <tt>Revision.diff()</tt>, denotes the previous revision.
     *  @see org.wikipedia.Wiki.Revision#diff(org.wikipedia.Wiki.Revision)
     *  @since 0.21
     */
    public static final long PREVIOUS_REVISION = -3L;

    /**
     *  The list of options the user can specify for his/her gender.
     *  @since 0.24
     */
    public enum Gender
    {
        // These names come from the MW API so we can use valueOf() and
        // toString() without any fidgets whatsoever. Java naming conventions
        // aren't worth another 20 lines of code.

        /**
         *  The user self-identifies as a male.
         *  @since 0.24
         */
        male,

        /**
         *  The user self-identifies as a female.
         *  @since 0.24
         */
        female,

        /**
         *  The user has not specified a gender in preferences.
         *  @since 0.24
         */
        unknown;
    }

    private static final String version = "0.31";

    // the domain of the wiki
    private String domain;
    protected String query, base, apiUrl;
    protected String scriptPath = "/w";
    private boolean wgCapitalLinks = true;
    private String timezone = "UTC";

    // user management
    private Map<String, String> cookies = new HashMap<>(12);
    private User user;
    private int statuscounter = 0;

    // various caches
    private transient LinkedHashMap<String, Integer> namespaces = null;
    private transient List<String> watchlist = null;

    // preferences
    private int max = 500;
    private int slowmax = 50;
    private int throttle = 10000; // throttle
    private int maxlag = 5;
    private int assertion = ASSERT_NONE; // assertion mode
    private transient int statusinterval = 100; // status check
    private String useragent = "Wiki.java/" + version + " (https://github.com/MER-C/wiki-java/)";
    private boolean zipped = true;
    private boolean markminor = false, markbot = false;
    private boolean resolveredirect = false;
    private String protocol = "https://";
    private Level loglevel = Level.ALL;
    private static final Logger logger = Logger.getLogger("wiki");

    // Store time when the last throttled action was executed
    private long lastThrottleActionTime = 0;
    // Store time when next login attempt can be executed
    private long nextLoginTime = -1;

    // retry flag
    private boolean retry = true;

    // serial version
    private static final long serialVersionUID = -8745212681497643456L;

    // time to open a connection
    private static final int CONNECTION_CONNECT_TIMEOUT_MSEC = 30000; // 30 seconds
    // time for the read to take place. (needs to be longer, some connections are slow
    // and the data volume is large!)
    private static final int CONNECTION_READ_TIMEOUT_MSEC = 180000; // 180 seconds
    // log2(upload chunk size). Default = 22 => upload size = 4 MB. Disable
    // chunked uploads by setting a large value here (50 = 1 PB will do).
    private static final int LOG2_CHUNK_SIZE = 22;

    // CONSTRUCTORS AND CONFIGURATION

    /**
     *  Creates a new connection to the English Wikipedia via HTTPS.
     *  @since 0.02
     */
    public Wiki()
    {
        this("en.wikipedia.org", "/w");
    }

    /**
     *  Creates a new connection to a wiki via HTTPS. WARNING: if the wiki uses
     *  a $wgScriptpath other than the default <tt>/w</tt>, you need to call
     *  <tt>getScriptPath()</tt> to automatically set it. Alternatively, you
     *  can use the constructor below if you know it in advance.
     *
     *  @param domain the wiki domain name e.g. en.wikipedia.org (defaults to
     *  en.wikipedia.org)
     */
    public Wiki(String domain)
    {
        this(domain, "/w");
    }

    /**
     *  Creates a new connection to a wiki with $wgScriptpath set to
     *  <tt>scriptPath</tt> via HTTPS.
     *
     *  @param domain the wiki domain name
     *  @param scriptPath the script path
     *  @since 0.14
     */
    public Wiki(String domain, String scriptPath)
    {
        this(domain, scriptPath, "https://");
    }

    /**
     *  Creates a new connection to a wiki with $wgScriptpath set to
     *  <tt>scriptPath</tt> via the specified protocol.
     *
     *  @param domain the wiki domain name
     *  @param scriptPath the script path
     *  @param protocol a protocol e.g. "http://", "https://" or "file:///"
     *  @since 0.31
     */
    public Wiki(String domain, String scriptPath, String protocol)
    {
        if (domain == null || domain.isEmpty())
            domain = "en.wikipedia.org";
        this.domain = domain;
        this.scriptPath = scriptPath;
        this.protocol = protocol;

        // init variables
        // This is fine as long as you do not have parameters other than domain
        // and scriptpath in constructors and do not do anything else than super(x)!
        // http://stackoverflow.com/questions/3404301/whats-wrong-with-overridable-method-calls-in-constructors
        // TODO: make this more sane.
        logger.setLevel(loglevel);
        log(Level.CONFIG, "<init>", "Using Wiki.java " + version);
        initVars();
    }

    /**
     *  Edit this if you need to change the API and human interface url
     *  configuration of the wiki. One example use is server-side cache 
     *  management (maxage and smaxage API parameters).
     *
     *  <p>Contributed by Tedder
     *  @since 0.24
     */
    protected void initVars()
    {
        StringBuilder basegen = new StringBuilder(protocol);
        basegen.append(domain);
        basegen.append(scriptPath);
        StringBuilder apigen = new StringBuilder(basegen);
        apigen.append("/api.php?format=xml&rawcontinue=1&");
        // MediaWiki has inbuilt maxlag functionality, see [[mw:Manual:Maxlag
        // parameter]]. Let's exploit it.
        if (maxlag >= 0)
        {
            apigen.append("maxlag=");
            apigen.append(maxlag);
            apigen.append("&");
            basegen.append("/index.php?maxlag=");
            basegen.append(maxlag);
            basegen.append("&title=");
        }
        else
            basegen.append("/index.php?title=");
        base = basegen.toString();
        // the native API supports assertions as of MW 1.23
        if ((assertion & ASSERT_BOT) == ASSERT_BOT)
            apigen.append("assert=bot&");
        else if ((assertion & ASSERT_USER) == ASSERT_USER)
            apigen.append("assert=user&");
        apiUrl = apigen.toString();
        apigen.append("action=query&");
        if (resolveredirect)
            apigen.append("redirects&");
        query = apigen.toString();
    }

    /**
     *  Gets the domain of the wiki, as supplied on construction.
     *  @return the domain of the wiki
     *  @since 0.06
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     *  Gets the editing throttle.
     *  @return the throttle value in milliseconds
     *  @see #setThrottle
     *  @since 0.09
     */
    public int getThrottle()
    {
        return throttle;
    }

    /**
     *  Sets the editing throttle. Read requests are not throttled or restricted
     *  in any way. Default is 10s.
     *  @param throttle the new throttle value in milliseconds
     *  @see #getThrottle
     *  @since 0.09
     */
    public void setThrottle(int throttle)
    {
        this.throttle = throttle;
        log(Level.CONFIG, "setThrottle", "Throttle set to " + throttle + " milliseconds");
    }

    /**
     *  Detects the $wgScriptpath wiki variable and sets the bot framework up
     *  to use it. You need not call this if you know the script path is
     *  <tt>/w</tt>. See also [[mw:Manual:$wgScriptpath]].
     *
     *  @throws IOException if a network error occurs
     *  @deprecated use getSiteInfo
     *  @return the script path, if you have any use for it
     *  @since 0.14
     */
    @Deprecated
    public String getScriptPath() throws IOException
    {
        return (String)getSiteInfo().get("scriptpath");
    }

    /**
     *  Detects whether a wiki forces upper case for the first character in a
     *  title and sets the bot framework up to use it. Example: en.wikipedia =
     *  true, en.wiktionary = false. Default = true. See [[mw:Manual:$wgCapitalLinks]].
     *  @return see above
     *  @deprecated use getSiteInfo
     *  @throws IOException if a network error occurs
     *  @since 0.30
     */
    @Deprecated
    public boolean isUsingCapitalLinks() throws IOException
    {
        return (Boolean)getSiteInfo().get("usingcapitallinks");
    }

    /**
     *  Gets various properties of the wiki and sets the bot framework up to use
     *  them. Returns:
     *  <ul>
     *  <li><b>usingcapitallinks</b>: (Boolean) whether a wiki forces upper case
     *    for the title. Example: en.wikipedia = true, en.wiktionary = false.
     *    Default = true. See [[mw:Manual:$wgCapitalLinks]].
     *  <li><b>scriptpath</b>: (String) the $wgScriptpath wiki variable. Default
     *    = <tt>/w</tt>. See [[mw:Manual:$wgScriptpath]].
     *  <li><b>version</b>: (String) the MediaWiki version used for this wiki
     *  <li><b>timezone</b>: (String) the timezone the wiki is in, default = UTC
     *  </ul>
     *
     *  @return (see above)
     *  @since 0.30
     *  @throws IOException if a network error occurs
     */
    public Map<String, Object> getSiteInfo() throws IOException
    {
        Map<String, Object> ret = new HashMap<>();
        String line = fetch(query + "action=query&meta=siteinfo", "getSiteInfo");
        wgCapitalLinks = parseAttribute(line, "case", 0).equals("first-letter");
        ret.put("usingcapitallinks", wgCapitalLinks);
        scriptPath = parseAttribute(line, "scriptpath", 0);
        ret.put("scriptpath", scriptPath);
        timezone = parseAttribute(line, "timezone", 0);
        ret.put("timezone", timezone);
        ret.put("version", parseAttribute(line, "generator", 0));
        initVars();
        return ret;
    }

    /**
     *  Sets the user agent HTTP header to be used for requests. Default is
     *  "Wiki.java " + version.
     *  @param useragent the new user agent
     *  @since 0.22
     */
    public void setUserAgent(String useragent)
    {
        this.useragent = useragent;
    }

    /**
     *  Gets the user agent HTTP header to be used for requests. Default is
     *  "Wiki.java " + version.
     *  @return useragent the user agent
     *  @since 0.22
     */
    public String getUserAgent()
    {
        return useragent;
    }

    /**
     *  Enables/disables GZip compression for GET requests. Default: true.
     *  @param zipped whether we use GZip compression
     *  @since 0.23
     */
    public void setUsingCompressedRequests(boolean zipped)
    {
        this.zipped = zipped;
    }

    /**
     *  Checks whether we are using GZip compression for GET requests.
     *  Default: true.
     *  @return (see above)
     *  @since 0.23
     */
    public boolean isUsingCompressedRequests()
    {
        return zipped;
    }

    /**
     *  Checks whether API action=query dependencies automatically resolve
     *  redirects (default = false).
     *  @return (see above)
     *  @since 0.27
     */
    public boolean isResolvingRedirects()
    {
        return resolveredirect;
    }

    /**
     *  Sets whether API action=query dependencies automatically resolve
     *  redirects (default = false).
     *  @param b (see above)
     *  @since 0.27
     */
    public void setResolveRedirects(boolean b)
    {
        resolveredirect = b;
        initVars();
    }

    /**
     *  Sets whether edits are marked as bot by default (may be overridden
     *  specifically by edit()). Default = false. Works only if one has the
     *  required permissions.
     *  @param markbot (see above)
     *  @since 0.26
     */
    public void setMarkBot(boolean markbot)
    {
        this.markbot = markbot;
    }

    /**
     *  Are edits are marked as bot by default?
     *  @return whether edits are marked as bot by default
     *  @since 0.26
     */
    public boolean isMarkBot()
    {
        return markbot;
    }

    /**
     *  Sets whether edits are marked as minor by default (may be overridden
     *  specifically by edit()). Default = false.
     *  @param minor (see above)
     *  @since 0.26
     */
    public void setMarkMinor(boolean minor)
    {
        this.markminor = minor;
    }

    /**
     *  Are edits are marked as minor by default?
     *  @return whether edits are marked as minor by default
     *  @since 0.26
     */
    public boolean isMarkMinor()
    {
        return markminor;
    }

    /**
     *  Determines whether this wiki is equal to another object.
     *  @param obj the object to compare
     *  @return whether the domains of the wikis are equal
     *  @since 0.10
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Wiki))
            return false;
        return domain.equals(((Wiki)obj).domain);
    }

    /**
     *  Returns a hash code of this object.
     *  @return a hash code
     *  @since 0.12
     */
    @Override
    public int hashCode()
    {
        return domain.hashCode() * maxlag - throttle;
    }

    /**
     *   Returns a string representation of this Wiki.
     *   @return a string representation of this Wiki.
     *   @since 0.10
     */
    @Override
    public String toString()
    {
        // domain
        StringBuilder buffer = new StringBuilder("Wiki[domain=");
        buffer.append(domain);

        // user
        buffer.append(",user=");
        buffer.append(user != null ? user.toString() : "null");
        buffer.append(",");

        // throttle mechanisms
        buffer.append("throttle=");
        buffer.append(throttle);
        buffer.append(",maxlag=");
        buffer.append(maxlag);
        buffer.append(",assertionMode=");
        buffer.append(assertion);
        buffer.append(",statusCheckInterval=");
        buffer.append(statusinterval);
        buffer.append(",cookies=");
        buffer.append(cookies);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     *  Gets the maxlag parameter. See [[mw:Manual:Maxlag parameter]].
     *  @return the current maxlag, in seconds
     *  @see #setMaxLag
     *  @see #getCurrentDatabaseLag
     *  @since 0.11
     */
    public int getMaxLag()
    {
        return maxlag;
    }

    /**
     *  Sets the maxlag parameter. A value of less than 0s disables this
     *  mechanism. Default is 5s.
     *  @param lag the desired maxlag in seconds
     *  @see #getMaxLag
     *  @see #getCurrentDatabaseLag
     *  @since 0.11
     */
    public void setMaxLag(int lag)
    {
        maxlag = lag;
        log(Level.CONFIG, "setMaxLag", "Setting maximum allowable database lag to " + lag);
        initVars();
    }

    /**
     *  Gets the assertion mode. Assertion modes are bitmasks.
     *  @return the current assertion mode
     *  @see #setAssertionMode
     *  @since 0.11
     */
    public int getAssertionMode()
    {
        return assertion;
    }

    /**
     *  Sets the assertion mode. Do this AFTER logging in, otherwise the login
     *  will fail. Assertion modes are bitmasks. Default is <tt>ASSERT_NONE</tt>.
     *  @param mode an assertion mode
     *  @see #getAssertionMode
     *  @since 0.11
     */
    public void setAssertionMode(int mode)
    {
        assertion = mode;
        log(Level.CONFIG, "setAssertionMode", "Set assertion mode to " + mode);
        initVars();
    }

    /**
     *  Gets the number of actions (edit, move, block, delete, etc) between
     *  status checks. A status check is where we update user rights, block
     *  status and check for new messages (if the appropriate assertion mode
     *  is set).
     *
     *  @return the number of edits between status checks
     *  @see #setStatusCheckInterval
     *  @since 0.18
     */
    public int getStatusCheckInterval()
    {
        return statusinterval;
    }

    /**
     *  Sets the number of actions (edit, move, block, delete, etc) between
     *  status checks. A status check is where we update user rights, block
     *  status and check for new messages (if the appropriate assertion mode
     *  is set). Default is 100.
     *
     *  @param interval the number of edits between status checks
     *  @see #getStatusCheckInterval
     *  @since 0.18
     */
    public void setStatusCheckInterval(int interval)
    {
        statusinterval = interval;
        log(Level.CONFIG, "setStatusCheckInterval", "Status check interval set to " + interval);
    }

    /**
     *  Set the logging level used by the internal logger.
     *  @param loglevel one of the levels specified in java.util.logging.LEVEL
     *  @since 0.31
     */
    public void setLogLevel(Level loglevel)
    {
        this.loglevel = loglevel;
        logger.setLevel(loglevel);
    }

    // META STUFF

    /**
     *  Logs in to the wiki. This method is thread-safe. You can set
     *  ignoreRateLimit to false (default: true), if you don't want the API
     *  to take care of throttling due to numerous failed login attempts.
     *
     *  @param username a username
     *  @param password a password (as a char[] due to JPasswordField)
     *  @param rateLimit makes this call "blocking", i.e. does obey rate limits
     *  @throws FailedLoginException if the login failed due to incorrect
     *  username and/or password and/or rate limits; Refer to the Exception's
     *  message to get the reason
     *  @throws IOException if a network error occurs
     *  @see #logout
     */
    public synchronized void login(String username, char[] password, boolean rateLimit) throws IOException, FailedLoginException
    {
        if (rateLimit)
        {
            long sleepDuration = nextLoginTime - System.currentTimeMillis();
            if (sleepDuration > 0)
            {
                // TODO log sleepDuration?
                try
                {
                    Thread.sleep(sleepDuration);
                }
                catch (InterruptedException ignore)
                {
                }
            }
        }
        // post login request
        username = normalize(username);
        StringBuilder buffer = new StringBuilder(500);
        buffer.append("lgname=");
        buffer.append(encode(username, false));
        // fetch token
        String response = post(apiUrl + "action=login", buffer.toString(), "login");
        String wpLoginToken = parseAttribute(response, "token", 0);
        buffer.append("&lgpassword=");
        buffer.append(encode(new String(password), false));
        buffer.append("&lgtoken=");
        buffer.append(encode(wpLoginToken, false));
        String line = post(apiUrl + "action=login", buffer.toString(), "login");
        buffer = null;

        // check for success
        if (line.contains("result=\"Success\""))
        {
            user = new User(username);
            boolean apihighlimit = user.isAllowedTo("apihighlimits");
            if (apihighlimit)
            {
                max = 5000;
                slowmax = 500;
            }
            log(Level.INFO, "login", "Successfully logged in as " + username + ", highLimit = " + apihighlimit);
        }
        else
        {
            log(Level.WARNING, "login", "Failed to log in as " + username);
            // test for some common failure reasons
            if (line.contains("WrongPass") || line.contains("WrongPluginPass"))
                throw new FailedLoginException("Login failed: incorrect password.");
            else if (line.contains("NotExists"))
                throw new FailedLoginException("Login failed: user does not exist.");
            else if (line.contains("Throttled"))
            {
                int wait = new Integer(parseAttribute(line, "wait", 0));
                nextLoginTime = System.currentTimeMillis() + 1000 * wait;
                throw new FailedLoginException("Login failed: throttled (wait: "
                    + wait + ")");
            }
            throw new FailedLoginException("Login failed: unknown reason.");
        }
    }

    /**
     *  Logs in to the wiki. This method is thread-safe. You can set
     *  ignoreRateLimit to false (default: true), if you don't want the API
     *  to take care of throttling due to numerous failed login attempts.
     *
     *  @param username a username
     *  @param password a string with the password
     *  @param rateLimit makes this call "blocking", i.e. does obey rate limits
     *  @throws FailedLoginException if the login failed due to incorrect
     *  username and/or password and/or rate limits; Refer to the Exception's
     *  message to get the reason
     *  @throws IOException if a network error occurs
     *  @see #logout
     */
    public synchronized void login(String username, String password, boolean rateLimit) throws IOException, FailedLoginException
    {
        login(username, password.toCharArray(), rateLimit);
    }

    /**
     *  Logs in to the wiki. This method is thread-safe.
     *
     *  @param username a username
     *  @param password a string with the password
     *  @throws FailedLoginException if the login failed due to incorrect
     *  username and/or password and/or rate limits; Refer to the Exception's
     *  message to get the reason
     *  @throws IOException if a network error occurs
     *  @see #logout
     */
    public synchronized void login(String username, String password) throws IOException, FailedLoginException
    {
        login(username, password.toCharArray(), true);
    }

    /**
     *  Logs in to the wiki. This method is thread-safe.
     *
     *  @param username a username
     *  @param password a password
     *  @throws FailedLoginException if the login failed due to incorrect
     *  username and/or password and/or rate limits; Refer to the Exception's
     *  message to get the reason
     *  @throws IOException if a network error occurs
     *  @see #logout
     */
    public synchronized void login(String username, char[] password) throws IOException, FailedLoginException
    {
        login(username, password, true);
    }

    /**
     *  Logs out of the wiki. This method is thread safe (so that we don't log
     *  out during an edit). All operations are conducted offline, so you can
     *  serialize this Wiki first.
     *  @see #login
     *  @see #logoutServerSide
     */
    public synchronized void logout()
    {
        cookies.clear();
        user = null;
        max = 500;
        slowmax = 50;
        log(Level.INFO, "logout", "Logged out");
    }

    /**
     *  Logs out of the wiki and destroys the session on the server. You will
     *  need to log in again instead of just reading in a serialized wiki.
     *  Equivalent to [[Special:Userlogout]]. This method is thread safe
     *  (so that we don't log out during an edit). WARNING: kills all
     *  concurrent sessions - if you are logged in with a browser this will log
     *  you out there as well.
     *
     *  @throws IOException if a network error occurs
     *  @since 0.14
     *  @see #login
     *  @see #logout
     */
    public synchronized void logoutServerSide() throws IOException
    {
        fetch(apiUrl + "action=logout", "logoutServerSide");
        logout(); // destroy local cookies
    }

    /**
     *  Determines whether the current user has new messages. (A human would
     *  notice a yellow bar at the top of the page).
     *  @return whether the user has new messages
     *  @throws IOException if a network error occurs
     *  @since 0.11
     */
    public boolean hasNewMessages() throws IOException
    {
        String url = query + "meta=userinfo&uiprop=hasmsg";
        return fetch(url, "hasNewMessages").contains("messages=\"\"");
    }

    /**
     *  Determines the current database replication lag.
     *  @return the current database replication lag
     *  @throws IOException if a network error occurs
     *  @see #setMaxLag
     *  @see #getMaxLag
     *  @since 0.10
     */
    public int getCurrentDatabaseLag() throws IOException
    {
        String line = fetch(query + "meta=siteinfo&siprop=dbrepllag", "getCurrentDatabaseLag");
        String lag = parseAttribute(line, "lag", 0);
        log(Level.INFO, "getCurrentDatabaseLag", "Current database replication lag is " + lag + " seconds");
        return Integer.parseInt(lag);
    }

    /**
     *  Fetches some site statistics, namely the number of articles, pages,
     *  files, edits, users and admins. Equivalent to [[Special:Statistics]].
     *
     *  @return a map containing the stats. Use "articles", "pages", "files"
     *  "edits", "users", "activeusers", "admins" or "jobs" to retrieve the
     *  respective value
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    public Map<String, Integer> getSiteStatistics() throws IOException
    {
        String text = fetch(query + "meta=siteinfo&siprop=statistics", "getSiteStatistics");
        Map<String, Integer> ret = new HashMap<>(20);
        ret.put("pages", Integer.parseInt(parseAttribute(text, "pages", 0)));
        ret.put("articles", Integer.parseInt(parseAttribute(text, "articles", 0)));
        ret.put("files", Integer.parseInt(parseAttribute(text, "images", 0)));
        ret.put("users", Integer.parseInt(parseAttribute(text, "users", 0)));
        ret.put("activeusers", Integer.parseInt(parseAttribute(text, "activeusers", 0)));
        ret.put("admins", Integer.parseInt(parseAttribute(text, "admins", 0)));
        ret.put("jobs", Integer.parseInt(parseAttribute(text, "jobs", 0))); // job queue length
        return ret;
    }

    /**
     *  Gets the version of MediaWiki this wiki runs e.g. 1.20wmf5 (54b4fcb).
     *  See also https://gerrit.wikimedia.org/ .
     *  @return the version of MediaWiki used
     *  @throws IOException if a network error occurs
     *  @deprecated use getSiteInfo
     *  @since 0.14
     */
    @Deprecated
    public String version() throws IOException
    {
        return (String)getSiteInfo().get("version");
    }

    /**
     *  Renders the specified wiki markup by passing it to the MediaWiki
     *  parser through the API. (Note: this isn't implemented locally because
     *  I can't be stuffed porting Parser.php). One use of this method is to
     *  emulate the previewing functionality of the MediaWiki software.
     *
     *  @param markup the markup to parse
     *  @return the parsed markup as HTML
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public String parse(String markup) throws IOException
    {
        // This is POST because markup can be arbitrarily large, as in the size
        // of an article (over 10kb).
        String response = post(apiUrl + "action=parse", "prop=text&text=" + encode(markup, false), "parse");
        int y = response.indexOf('>', response.indexOf("<text")) + 1;
        int z = response.indexOf("</text>");
        return decode(response.substring(y, z));
    }

    /**
     *  Same as <tt>parse()</tt>, but also strips out unwanted crap. This might
     *  be useful to subclasses.
     *
     *  @param in the string to parse
     *  @return that string without the crap
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    protected String parseAndCleanup(String in) throws IOException
    {
        String output = parse(in);
        output = output.replace("<p>", "").replace("</p>", ""); // remove paragraph tags
        output = output.replace("\n", ""); // remove new lines

        // strip out the parser report, which comes at the end
        int a = output.indexOf("<!--");
        return output.substring(0, a);
    }

    /**
     *  Fetches a random page in the main namespace. Equivalent to
     *  [[Special:Random]].
     *  @return the title of the page
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public String random() throws IOException
    {
        return random(MAIN_NAMESPACE);
    }

    /**
     *  Fetches a random page in the specified namespace. Equivalent to
     *  [[Special:Random]].
     *
     *  @param ns namespace(s)
     *  @return the title of the page
     *  @throws IOException if a network error occurs
     *  @since 0.13
     */
    public String random(int... ns) throws IOException
    {
        // no bulk queries here because they are deterministic -- [[mw:API:Random]]
        StringBuilder url = new StringBuilder(query);
        url.append("list=random");
        constructNamespaceString(url, "rn", ns);
        String line = fetch(url.toString(), "random");
        return parseAttribute(line, "title", 0);
    }
    
    /**
     *  Fetches edit and other types of tokens.
     *  @param type one of "csrf", "patrol", "rollback", "userrights", "watch"
     *  @return the token
     *  @throws IOException if a network error occurs
     *  @since 0.32
     */
    public String getToken(String type) throws IOException
    {
        String content = fetch(query + "meta=tokens&type=" + type, "getToken");
        String token = parseAttribute(content, type + "token", 0);
        
        // this is a good chance to check whether we are still logged in
        //if (token.equals("\\+"))
        //{
        //    log(Level.SEVERE, "emailUser", "Cookies have expired.");
        //    logout();
        //    throw new CredentialExpiredException("Cookies have expired.");
        //}
        return token;
    }
    
    // STATIC MEMBERS

    /**
     *  Determines the intersection of two lists of pages a and b.
     *  Such lists might be generated from the various list methods below.
     *  Examples from the English Wikipedia:
     *
     *  <pre>
     *  // find all orphaned and unwikified articles
     *  String[] articles = Wiki.intersection(wikipedia.getCategoryMembers("All orphaned articles", Wiki.MAIN_NAMESPACE),
     *      wikipedia.getCategoryMembers("All pages needing to be wikified", Wiki.MAIN_NAMESPACE));
     *
     *  // find all (notable) living people who are related to Barack Obama
     *  String[] people = Wiki.intersection(wikipedia.getCategoryMembers("Living people", Wiki.MAIN_NAMESPACE),
     *      wikipedia.whatLinksHere("Barack Obama", Wiki.MAIN_NAMESPACE));
     *  </pre>
     *
     *  @param a a list of pages
     *  @param b another list of pages
     *  @return a intersect b (as String[])
     *  @since 0.04
     */
    public static String[] intersection(String[] a, String[] b)
    {
        // @revised 0.11 to take advantage of Collection.retainAll()
        // @revised 0.14 genericised to all page titles, not just category members

        List<String> intersec = new ArrayList<>(5000); // silly workaround
        intersec.addAll(Arrays.asList(a));
        intersec.retainAll(Arrays.asList(b));
        return intersec.toArray(new String[intersec.size()]);
    }

    /**
     *  Determines the list of articles that are in a but not b, i.e. a \ b.
     *  This is not the same as b \ a. Such lists might be generated from the
     *  various lists below. Some examples from the English Wikipedia:
     *
     *  <pre>
     *  // find all Martian crater articles that do not have an infobox
     *  String[] articles = Wiki.relativeComplement(wikipedia.getCategoryMembers("Craters on Mars"),
     *      wikipedia.whatTranscludesHere("Template:MarsGeo-Crater", Wiki.MAIN_NAMESPACE));
     *
     *  // find all images without a description that haven't been tagged "no license"
     *  String[] images = Wiki.relativeComplement(wikipedia.getCategoryMembers("Images lacking a description"),
     *      wikipedia.getCategoryMembers("All images with unknown copyright status"));
     *  </pre>
     *
     *  @param a a list of pages
     *  @param b another list of pages
     *  @return a \ b
     *  @since 0.14
     */
    public static String[] relativeComplement(String[] a, String[] b)
    {
        List<String> compl = new ArrayList<>(5000); // silly workaround
        compl.addAll(Arrays.asList(a));
        compl.removeAll(Arrays.asList(b));
        return compl.toArray(new String[compl.size()]);
    }

    // PAGE METHODS

    /**
     *  Returns the corresponding talk page to this page.
     *
     *  @param title the page title
     *  @return the name of the talk page corresponding to <tt>title</tt>
     *  or "" if we cannot recognise it
     *  @throws IllegalArgumentException if given title is in a talk namespace
     *  or we try to retrieve the talk page of a Special: or Media: page.
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String getTalkPage(String title) throws IOException
    {
        // It is convention that talk namespaces are the original namespace + 1
        // and are odd integers.
        int namespace = namespace(title);
        if (namespace % 2 == 1)
            throw new IllegalArgumentException("Cannot fetch talk page of a talk page!");
        if (namespace < 0)
            throw new IllegalArgumentException("Special: and Media: pages do not have talk pages!");
        if (namespace != MAIN_NAMESPACE) // remove the namespace
            title = title.substring(title.indexOf(':') + 1);
        return namespaceIdentifier(namespace + 1) + ":" + title;
    }

    /**
     *  Gets miscellaneous page info.
     *  @param page the page to get info for
     *  @return see {@link #getPageInfo(String[]) }
     *  @throws IOException if a network error occurs
     *  @since 0.28
     */
    public Map getPageInfo(String page) throws IOException
    {
        return getPageInfo(new String[] { page })[0];
    }

    /**
     *  Gets miscellaneous page info. Returns:
     *  <ul>
     *  <li><b>displaytitle</b>: (String) the title of the page that is actually
     *    displayed. Example: "iPod"
     *  <li><b>protection</b>: (Map) the {@link #protect(java.lang.String,
     *    java.util.Map, java.lang.String) protection state} of the page. Does
     *    not cover implied protection levels (e.g. MediaWiki namespace).
     *  <li><b>token</b>: (String) an edit token for the page, must be logged in
     *    to be non-trivial. <b>DEPRECATED</b>: use getToken() instead.
     *  <li><b>exists</b>: (Boolean) whether the page exists
     *  <li><b>lastpurged</b>: (Calendar) when the page was last purged or null
     *    if the page does not exist
     *  <li><b>lastrevid</b>: (Long) the revid of the top revision or -1L if the
     *    page does not exist
     *  <li><b>size</b>: (Integer) the size of the page or -1 if the page does
     *    not exist
     *  <li><b>pageid</b>: (Long) the id of the page or -1 if the page does not
     *    exist
     *  <li><b>timestamp</b>: (Calendar) when this method was called
     *  <li><b>watchtoken</b>: (String) watchlist token or null if logged out.
     *  <b>DEPRECATED</b>: use getToken() instead.
     *  <li><b>watchers</b>: (Integer) number of watchers, may be restricted
     *  </ul>
     *
     *  @param pages the pages to get info for.
     *  @return (see above). The Maps will come out in the same order as the
     *  processed array.
     *  @throws IOException if a network error occurs
     *  @since 0.23
     */
    public Map[] getPageInfo(String[] pages) throws IOException
    {
        Map[] info = new HashMap[pages.length];
        StringBuilder url = new StringBuilder(query);
        url.append("prop=info&intoken=edit%7Cwatch&inprop=protection%7Cdisplaytitle%7Cwatchers&titles=");
        String[] titles = constructTitleString(pages);
        for (String temp : titles)
        {
            String line = fetch(url.toString() + temp, "getPageInfo");

            // form: <page pageid="239098" ns="0" title="BitTorrent" ... >
            // <protection />
            // </page>
            for (int j = line.indexOf("<page "); j > 0; j = line.indexOf("<page ", ++j))
            {
                int x = line.indexOf("</page>", j);
                String item = line.substring(j, x);
                Map<String, Object> tempmap = new HashMap<>(15);

                // does the page exist?
                boolean exists = !item.contains("missing=\"\"");
                tempmap.put("exists", exists);
                if (exists)
                {
                    tempmap.put("lastpurged", timestampToCalendar(parseAttribute(item, "touched", 0), true));
                    tempmap.put("lastrevid", Long.parseLong(parseAttribute(item, "lastrevid", 0)));
                    tempmap.put("size", Integer.parseInt(parseAttribute(item, "length", 0)));
                    tempmap.put("pageid", Long.parseLong(parseAttribute(item, "pageid", 0)));
                }
                else
                {
                    tempmap.put("lastedited", null);
                    tempmap.put("lastrevid", -1L);
                    tempmap.put("size", -1);
                    tempmap.put("pageid", -1);
                }

                // parse protection level
                // expected form: <pr type="edit" level="sysop" expiry="infinity" cascade="" />
                Map<String, Object> protectionstate = new HashMap<>();
                for (int z = item.indexOf("<pr "); z > 0; z = item.indexOf("<pr ", ++z))
                {
                    String type = parseAttribute(item, "type", z);
                    String level = parseAttribute(item, "level", z);
                    protectionstate.put(type, level);
                    //if (level != NO_PROTECTION)
                    String expiry = parseAttribute(item, "expiry", z);
                    if (expiry.equals("infinity"))
                        protectionstate.put(type + "expiry", null);
                    else
                        protectionstate.put(type + "expiry", timestampToCalendar(expiry, true));
                    // protected via cascade
                    if (item.contains("source=\""))
                        protectionstate.put("cascadesource", parseAttribute(item, "source", z));
                }
                // MediaWiki namespace
                String parsedtitle = parseAttribute(item, "title", 0);
                if (namespace(parsedtitle) == MEDIAWIKI_NAMESPACE)
                {
                    protectionstate.put("edit", FULL_PROTECTION);
                    protectionstate.put("move", FULL_PROTECTION);
                    if (!exists)
                        protectionstate.put("create", FULL_PROTECTION);
                }

                protectionstate.put("cascade", item.contains("cascade=\"\""));
                tempmap.put("protection", protectionstate);

                tempmap.put("displaytitle", parseAttribute(item, "displaytitle", 0));
                tempmap.put("timestamp", makeCalendar());

                // DEPRECATED, will be removed shortly
                tempmap.put("token", parseAttribute(item, "edittoken", 0)); 
                // watchlist token
                if (user != null)
                {
                    tempmap.put("watchtoken", parseAttribute(item, "watchtoken", 0));
                    logger.log(Level.WARNING, "getPageInfo: watchtoken and editoken are deprecated and will be removed shortly.");
                }

                // number of watchers
                if (item.contains("watchers=\""))
                    tempmap.put("watchers", Integer.parseInt(parseAttribute(item, "watchers", 0)));

                // reorder
                for (int i = 0; i < pages.length; i++)
                    if (normalize(pages[i]).equals(parsedtitle))
                        info[i] = tempmap;
            }
        }

        log(Level.INFO, "getPageInfo", "Successfully retrieved page info for " + Arrays.toString(pages));
        return info;
    }

    /**
     *  Returns the namespace a page is in. No need to override this to
     *  add custom namespaces, though you may want to define static fields e.g.
     *  <tt>public static final int PORTAL_NAMESPACE = 100;</tt> for the Portal
     *  namespace on the English Wikipedia.
     *
     *  @param title any valid page name
     *  @return an integer array containing the namespace of <tt>title</tt>
     *  @throws IOException if a network error occurs while populating the
     *  namespace cache
     *  @see #namespaceIdentifier(int)
     *  @since 0.03
     */
    public int namespace(String title) throws IOException
    {
        // cache this, as it will be called often
        if (namespaces == null)
            populateNamespaceCache();

        // perform a limited normalization
        if (title.startsWith(":"))
            title = title.substring(1);
        if (!title.contains(":"))
            return MAIN_NAMESPACE;
        title = title.replace("_", " ");
        String namespace = title.substring(0, 1).toUpperCase() + title.substring(1, title.indexOf(':'));

        // look up the namespace of the page in the namespace cache
        if (!namespaces.containsKey(namespace))
            return MAIN_NAMESPACE; // For titles like UN:NRV
        else
            return namespaces.get(namespace);
    }

    /**
     *  For a given namespace denoted as an integer, fetch the corresponding
     *  identification string e.g. <tt>namespaceIdentifier(1)</tt> should return
     *  "Talk" on en.wp. (This does the exact opposite to <tt>namespace()</tt>).
     *  Strings returned are always localized.
     *
     *  @param namespace an integer corresponding to a namespace. If it does not
     *  correspond to a namespace, we assume you mean the main namespace (i.e.
     *  return "").
     *  @return the identifier of the namespace
     *  @throws IOException if the namespace cache has not been populated, and
     *  a network error occurs when populating it
     *  @see #namespace(java.lang.String)
     *  @since 0.25
     */
    public String namespaceIdentifier(int namespace) throws IOException
    {
        if (namespaces == null)
            populateNamespaceCache();

        // anything we cannot identify is assumed to be in the main namespace
        if (!namespaces.containsValue(namespace))
            return "";
        for (Map.Entry<String, Integer> entry : namespaces.entrySet())
            if (entry.getValue().equals(namespace))
                return entry.getKey();
        return ""; // never reached...
    }

    /**
     *  Gets the namespaces used by this wiki.
     *  @return a map containing e.g. {"Media" : -2, "Special" : -1, ...}.
     *  Changes in this map do not propagate back to this Wiki object.
     *  @throws IOException if a network error occurs
     *  @since 0.28
     */
    public LinkedHashMap<String, Integer> getNamespaces() throws IOException
    {
        if (namespaces == null)
            populateNamespaceCache();
        return (LinkedHashMap<String, Integer>)namespaces.clone();
    }

    /**
     *  Populates the namespace cache.
     *  @throws IOException if a network error occurs.
     *  @since 0.25
     */
    protected void populateNamespaceCache() throws IOException
    {
        String line = fetch(query + "meta=siteinfo&siprop=namespaces%7Cnamespacealiases", "namespace");
        namespaces = new LinkedHashMap<>(30);

        // xml form: <ns id="-2" canonical="Media" ... >Media</ns> or <ns id="0" ... />
        for (int a = line.indexOf("<ns "); a > 0; a = line.indexOf("<ns ", ++a))
        {
            int ns = Integer.parseInt(parseAttribute(line, "id", a));
            int b = line.indexOf('>', a) + 1;
            int c = line.indexOf('<', b);
            // this must be first so that namespaceIdentifier always returns the
            // localized name
            namespaces.put(normalize(decode(line.substring(b, c))), ns);
            String canonicalnamespace = parseAttribute(line, "canonical", a);
            if (canonicalnamespace != null) // not present for main namespace
                namespaces.put(canonicalnamespace, ns);
        }

        log(Level.INFO, "namespace", "Successfully retrieved namespace list (" + namespaces.size() + " namespaces)");
    }

    /**
     *  Determines whether a series of pages exist.
     *  @param titles the titles to check.
     *  @return whether the pages exist, in the same order as the processed array
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public boolean[] exists(String[] titles) throws IOException
    {
        boolean[] ret = new boolean[titles.length];
        Map[] info = getPageInfo(titles);
        for (int i = 0; i < titles.length; i++)
            ret[i] = (Boolean)info[i].get("exists");
        return ret;
    }

    /**
     *  Gets the raw wikicode for a page. WARNING: does not support special
     *  pages. Check [[User talk:MER-C/Wiki.java#Special page equivalents]]
     *  for fetching the contents of special pages. Use <tt>getImage()</tt> to
     *  fetch an image.
     *
     *  @param title the title of the page.
     *  @return the raw wikicode of a page.
     *  @throws UnsupportedOperationException if you try to retrieve the text of a
     *  Special: or Media: page
     *  @throws FileNotFoundException if the page does not exist
     *  @throws IOException if a network error occurs
     *  @see #edit
     */
    public String getPageText(String title) throws IOException
    {
        // pitfall check
        if (namespace(title) < 0)
            throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");

        // go for it
        String url = base + encode(title, true) + "&action=raw";
        String temp = fetch(url, "getPageText");
        log(Level.INFO, "getPageText", "Successfully retrieved text of " + title);
        return temp;
    }

    /**
     *  Gets the text of a specific section. Useful for section editing.
     *  @param title the title of the relevant page
     *  @param number the section number of the section to retrieve text for
     *  @return the text of the given section
     *  @throws IOException if a network error occurs
     *  @throws UnknownError if the page has less than <tt>number</tt>
     *  sections
     *  @since 0.24
     */
    public String getSectionText(String title, int number) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=revisions&rvprop=content&titles=");
        url.append(encode(title, true));
        url.append("&rvsection=");
        url.append(number);
        String text = fetch(url.toString(), "getSectionText");
        // This is currently broken because fetch() intercepts the API error.
        // if (text.contains("code=\"rvnosuchsection\""))
        //    throw new IllegalArgumentException("There is no section " + number + " in the page " + title);
        // if the section does not contain any text, <rev xml:space=\"preserve\">
        // will not have a separate closing tag
        if (!text.contains("</rev>"))
            return "";
        int a = text.indexOf("<rev ");
        a = text.indexOf("xml:space=\"preserve\">", a) + 21;
        int b = text.indexOf("</rev>", a);
        return decode(text.substring(a, b));
    }

    /**
     *  Gets the contents of a page, rendered in HTML (as opposed to
     *  wikitext). WARNING: only supports special pages in certain
     *  circumstances, for example <tt>getRenderedText("Special:Recentchanges")
     *  </tt> returns the 50 most recent change to the wiki in pretty-print
     *  HTML. You should test any use of this method on-wiki through the text
     *  <tt>{{Special:Specialpage}}</tt>. Use <tt>getImage()</tt> to fetch an
     *  image. Be aware of any transclusion limits, as outlined at
     *  [[Wikipedia:Template limits]].
     *
     *  @param title the title of the page
     *  @return the rendered contents of that page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String getRenderedText(String title) throws IOException
    {
        // @revised 0.13 genericised to parse any wikitext
        return parse("{{:" + title + "}}");
    }

    /**
     *  Edits a page by setting its text to the supplied value. This method is
     *  thread safe and blocks for a minimum time as specified by the
     *  throttle. The edit will be marked bot if <tt>isMarkBot() == true</tt>
     *  and minor if <tt>isMarkMinor() == true</tt>.
     *
     *  @param text the text of the page
     *  @param title the title of the page
     *  @param summary the edit summary. See [[Help:Edit summary]]. Summaries
     *  longer than 200 characters are truncated server-side.
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to edit a Special: or a
     *  Media: page
     *  @see #getPageText
     */
    public void edit(String title, String text, String summary) throws IOException, LoginException
    {
        edit(title, text, summary, markminor, markbot, -2, null);
    }

    /**
     *  Edits a page by setting its text to the supplied value. This method is
     *  thread safe and blocks for a minimum time as specified by the
     *  throttle. The edit will be marked bot if <tt>isMarkBot() == true</tt>
     *  and minor if <tt>isMarkMinor() == true</tt>.
     *
     *  @param text the text of the page
     *  @param title the title of the page
     *  @param summary the edit summary. See [[Help:Edit summary]]. Summaries
     *  longer than 200 characters are truncated server-side.
     *  @param basetime the timestamp of the revision on which <tt>text</tt> is
     *  based, used to check for edit conflicts. <tt>null</tt> disables this.
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to edit a Special: or a
     *  Media: page
     *  @see #getPageText
     */
    public void edit(String title, String text, String summary, Calendar basetime) throws IOException, LoginException
    {
        edit(title, text, summary, markminor, markbot, -2, basetime);
    }

    /**
     *  Edits a page by setting its text to the supplied value. This method is
     *  thread safe and blocks for a minimum time as specified by the
     *  throttle. The edit will be marked bot if <tt>isMarkBot() == true</tt>
     *  and minor if <tt>isMarkMinor() == true</tt>.
     *
     *  @param text the text of the page
     *  @param title the title of the page
     *  @param summary the edit summary. See [[Help:Edit summary]]. Summaries
     *  longer than 200 characters are truncated server-side.
     *  @param section the section to edit. Use -1 to specify a new section and
     *  -2 to disable section editing.
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to edit a Special: or a
     *  Media: page
     *  @see #getPageText
     *  @since 0.25
     */
    public void edit(String title, String text, String summary, int section) throws IOException, LoginException
    {
        edit(title, text, summary, markminor, markbot, section, null);
    }

    /**
     *  Edits a page by setting its text to the supplied value. This method is
     *  thread safe and blocks for a minimum time as specified by the
     *  throttle. The edit will be marked bot if <tt>isMarkBot() == true</tt>
     *  and minor if <tt>isMarkMinor() == true</tt>.
     *
     *  @param text the text of the page
     *  @param title the title of the page
     *  @param summary the edit summary. See [[Help:Edit summary]]. Summaries
     *  longer than 200 characters are truncated server-side.
     *  @param section the section to edit. Use -1 to specify a new section and
     *  -2 to disable section editing.
     *  @param basetime the timestamp of the revision on which <tt>text</tt> is
     *  based, used to check for edit conflicts. <tt>null</tt> disables this.
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to edit a Special: or a
     *  Media: page
     *  @see #getPageText
     *  @since 0.25
     */
    public void edit(String title, String text, String summary, int section, Calendar basetime)
        throws IOException, LoginException
    {
        edit(title, text, summary, markminor, markbot, section, basetime);
    }

    /**
     *  Edits a page by setting its text to the supplied value. This method is
     *  thread safe and blocks for a minimum time as specified by the
     *  throttle.
     *
     *  @param text the text of the page
     *  @param title the title of the page
     *  @param summary the edit summary or the title of the new section. See
     *  [[Help:Edit summary]]. Summaries longer than 200 characters are
     *  truncated server-side.
     *  @param minor whether the edit should be marked as minor, See
     *  [[Help:Minor edit]].
     *  @param bot whether to mark the edit as a bot edit (ignored if one does
     *  not have the necessary permissions)
     *  @param section the section to edit. Use -1 to specify a new section and
     *  -2 to disable section editing.
     *  @param basetime the timestamp of the revision on which <tt>text</tt> is
     *  based, used to check for edit conflicts. <tt>null</tt> disables this.
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws UnsupportedOperationException if you try to edit a Special: or
     *  Media: page
     *  @see #getPageText
     *  @since 0.17
     */
    public synchronized void edit(String title, String text, String summary, boolean minor, boolean bot,
        int section, Calendar basetime) throws IOException, LoginException
    {
        // @revised 0.16 to use API edit. No more screenscraping - yay!
        // @revised 0.17 section editing
        // @revised 0.25 optional bot flagging
        throttle();

        // protection
        Map info = getPageInfo(title);
        if (!checkRights(info, "edit") || (Boolean)info.get("exists") && !checkRights(info, "create"))
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            log(Level.WARNING, "edit", "Cannot edit - permission denied. " + ex);
            throw ex;
        }

        // post data
        StringBuilder buffer = new StringBuilder(300000);
        buffer.append("title=");
        buffer.append(encode(title, true));
        buffer.append("&text=");
        buffer.append(encode(text, false));
        if (section != -1)
        {
            // edit summary created automatically if making a new section
            buffer.append("&summary=");
            buffer.append(encode(summary, false));
        }
        buffer.append("&token=");
        buffer.append(encode(getToken("csrf"), false));
        if (basetime != null)
        {
            buffer.append("&starttimestamp=");
            buffer.append(calendarToTimestamp((Calendar)info.get("timestamp")));
            buffer.append("&basetimestamp=");
            // I wonder if the time getPageText() was called suffices here
            buffer.append(calendarToTimestamp(basetime));
        }
        if (minor)
            buffer.append("&minor=1");
        if (bot && user != null && user.isAllowedTo("bot"))
            buffer.append("&bot=1");
        if (section == -1)
        {
            buffer.append("&section=new&sectiontitle=");
            buffer.append(encode(summary, false));
        }
        else if (section != -2)
        {
            buffer.append("&section=");
            buffer.append(section);
        }
        String response = post(apiUrl + "action=edit", buffer.toString(), "edit");

        // done
        if (response.contains("error code=\"editconflict\""))
        {
            log(Level.WARNING, "edit", "Edit conflict on " + title);
            return; // TODO throw an exception!
        }
        try
        {
            checkErrorsAndUpdateStatus(response, "edit");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "edit", "Exception: " + e.getMessage() + " Retrying...");
                edit(title, text, summary, minor, bot, section, basetime);
            }
            else
            {
                log(Level.SEVERE, "edit", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "edit", "Successfully edited " + title);
        retry = true;
    }

    /**
     *  Creates a new section on the specified page. Leave <tt>subject</tt> as
     *  the empty string if you just want to append.
     *
     *  @param title the title of the page to edit
     *  @param subject the subject of the new section
     *  @param text the text of the new section
     *  @param minor whether the edit should be marked as minor (see
     *  [[Help:Minor edit]])
     *  @param bot whether to mark the edit as a bot edit (ignored if one does
     *  not have the necessary permissions)
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws UnsupportedOperationException if you try to edit a Special: or
     *  Media: page
     *  @since 0.17
     */
    public void newSection(String title, String subject, String text, boolean minor, boolean bot) throws IOException, LoginException
    {
        edit(title, text, subject, minor, bot, -1, null);
    }

    /**
     *  Prepends something to the given page. A convenience method for
     *  adding maintainance templates, rather than getting and setting the
     *  page yourself.
     *
     *  @param title the title of the page
     *  @param stuff what to prepend to the page
     *  @param summary the edit summary. See [[Help:Edit summary]]. Summaries
     *  longer than 200 characters are truncated server-side.
     *  @param minor whether the edit is minor
     *  @param bot whether to mark the edit as a bot edit (ignored if one does
     *  not have the necessary permissions)
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws UnsupportedOperationException if you try to retrieve the text
     *  of a Special: page or a Media: page
     *  @throws IOException if a network error occurs
     */
    public void prepend(String title, String stuff, String summary, boolean minor, boolean bot) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        // section 0 to save bandwidth
        text.append(getSectionText(title, 0));
        edit(title, text.toString(), summary, minor, bot, 0, null);
    }

    /**
     *  Deletes a page. Does not delete any page requiring <tt>bigdelete</tt>.
     *  @param title the page to delete
     *  @param reason the reason for deletion
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if the user lacks the permission to
     *  delete
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws AccountLockedException if user is blocked
     *  @since 0.24
     */
    public synchronized void delete(String title, String reason) throws IOException, LoginException
    {
        throttle();
        if (user == null || !user.isAllowedTo("delete"))
            throw new CredentialNotFoundException("Cannot delete: Permission denied");

        // edit token
        Map info = getPageInfo(title);
        if (!(Boolean)info.get("exists"))
        {
            log(Level.INFO, "delete", "Page \"" + title + "\" does not exist.");
            return;
        }
        String deleteToken = (String)info.get("token");

        // post data
        StringBuilder buffer = new StringBuilder(500);
        buffer.append("title=");
        buffer.append(encode(title, true));
        buffer.append("&reason=");
        buffer.append(encode(reason, false));
        buffer.append("&token=");
        buffer.append(encode(getToken("csrf"), false));
        String response = post(apiUrl + "action=delete", buffer.toString(), "delete");

        // done
        try
        {
            if (!response.contains("<delete title="))
                checkErrorsAndUpdateStatus(response, "delete");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "delete", "Exception: " + e.getMessage() + " Retrying...");
                delete(title, reason);
            }
            else
            {
                log(Level.SEVERE, "delete", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "delete", "Successfully deleted " + title);
        retry = true;
    }

    /**
     *  Undeletes a page. Equivalent to [[Special:Undelete]]. Restores ALL deleted
     *  revisions and files by default. This method is throttled.
     *  @param title a page to undelete
     *  @param reason the reason for undeletion
     *  @param revisions a list of revisions for selective undeletion
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot undelete
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws AccountLockedException if user is blocked
     *  @since 0.30
     */
    public synchronized void undelete(String title, String reason, Revision... revisions) throws IOException, LoginException
    {
        throttle();
        if (user == null || !user.isAllowedTo("undelete"))
            throw new CredentialNotFoundException("Cannot undelete: Permission denied");

        StringBuilder out = new StringBuilder("title=");
        out.append(encode(title, true));
        out.append("&reason=");
        out.append(encode(reason, false));
        out.append("&token=");
        out.append(encode(getToken("csrf"), false));
        if (revisions.length != 0)
        {
            out.append("&timestamps=");
            for (int i = 0; i < revisions.length - 1; i++)
            {
                out.append(calendarToTimestamp(revisions[i].getTimestamp()));
                out.append("%7C");
            }
            out.append(calendarToTimestamp(revisions[revisions.length - 1].getTimestamp()));
        }
        String response = post(apiUrl + "action=undelete", out.toString(), "undelete");

        // done
        try
        {
            if (!response.contains("<undelete title="))
            {
                checkErrorsAndUpdateStatus(response, "undelete");
                if (response.contains("cantundelete"))
                    log(Level.WARNING, "undelete", "Can't undelete: " + title + " has no deleted revisions.");
            }            
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "undelete", "Exception: " + e.getMessage() + " Retrying...");
                undelete(title, reason, revisions);
            }
            else
            {
                log(Level.SEVERE, "undelete", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "undelete", "Successfully undeleted " + title);
        retry = true;
        for (Revision rev : revisions)
            rev.pageDeleted = false;
    }

    /**
     *  Purges the server-side cache for various pages.
     *  @param titles the titles of the page to purge
     *  @param links update the links tables
     *  @throws IOException if a network error occurs
     *  @since 0.17
     */
    public void purge(boolean links, String... titles) throws IOException
    {
        StringBuilder url = new StringBuilder(apiUrl);
        url.append("action=purge");
        if (links)
            url.append("&forcelinkupdate");
        String[] temp = constructTitleString(titles);
        for (String x : temp)
            post(url.toString(), "&titles=" + x, "purge");
        log(Level.INFO, "purge", "Successfully purged " + titles.length + " pages.");
    }

    /**
     *  Gets the list of images used on a particular page. If there are
     *  redirected images, both the source and target page are included. Capped
     *  at <tt>max</tt> number of images, there's no reason why there should be
     *  more than that.
     *
     *  @param title a page
     *  @return the list of images used in the page.  Note that each String in the array will begin with the
     *  prefix "File:"
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getImagesOnPage(String title) throws IOException
    {
        String url = query + "prop=images&imlimit=max&titles=" + encode(title, true);
        String line = fetch(url, "getImagesOnPage");

        // xml form: <im ns="6" title="File:Example.jpg" />
        List<String> images = new ArrayList<>(750);
        for (int a = line.indexOf("<im "); a > 0; a = line.indexOf("<im ", ++a))
            images.add(parseAttribute(line, "title", a));

        int temp = images.size();
        log(Level.INFO, "getImagesOnPage", "Successfully retrieved images used on " + title + " (" + temp + " images)");
        return images.toArray(new String[temp]);
    }

    /**
     *  Gets the list of categories a particular page is in. Includes hidden
     *  categories. Capped at <tt>max</tt> number of categories, there's no
     *  reason why there should be more than that.
     *
     *  @param title a page
     *  @return the list of categories that page is in
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getCategories(String title) throws IOException
    {
        return getCategories(title, false, false);
    }

    /**
     *  Gets the list of categories a particular page is in. Ignores hidden
     *  categories if ignoreHidden is true. Also includes the sortkey of a
     *  category if sortkey is true. The sortkey would then be appended to
     *  the element of the returned string array (separated by "|").
     *  Capped at <tt>max</tt> number of categories, there's no reason why
     *  there should be more than that.
     *
     *  @param title a page
     *  @param sortkey return a sortkey as well (default = false)
     *  @param ignoreHidden skip hidden categories (default = false)
     *  @return the list of categories that the page is in
     *  @throws IOException if a network error occurs
     *  @since 0.30
     */
    public String[] getCategories(String title, boolean sortkey, boolean ignoreHidden) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=categories&cllimit=max");
        if (sortkey || ignoreHidden)
            url.append("&clprop=sortkey%7Chidden");
        url.append("&titles=");
        url.append(encode(title, true));
        String line = fetch(url.toString(), "getCategories");

        // xml form: <cl ns="14" title="Category:1879 births" sortkey=(long string) sortkeyprefix="" />
        // or      : <cl ns="14" title="Category:Images for cleanup" sortkey=(long string) sortkeyprefix="Borders" hidden="" />
        List<String> categories = new ArrayList<>(750);
        int a, b; // beginIndex and endIndex
        for (a = line.indexOf("<cl "); a > 0; a = b)
        {
            b = line.indexOf("<cl ", a + 1);
            if (ignoreHidden && line.substring(a, (b > 0 ? b : line.length())).contains("hidden"))
                continue;
            String category = parseAttribute(line, "title", a);
            if (sortkey)
                category += ("|" + parseAttribute(line, "sortkeyprefix", a));
            categories.add(category);
        }
        int temp = categories.size();
        log(Level.INFO, "getCategories", "Successfully retrieved categories of " + title + " (" + temp + " categories)");
        return categories.toArray(new String[temp]);
    }

    /**
     *  Gets the list of templates used on a particular page that are in a
     *  particular namespace(s). Capped at <tt>max</tt> number of templates,
     *  there's no reason why there should be more than that.
     *
     *  @param title a page
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the list of templates used on that page in that namespace
     *  @throws IOException if a network error occurs
     *  @since 0.16
     */
    public String[] getTemplates(String title, int... ns) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=templates&tllimit=max&titles=");
        url.append(encode(title, true));
        constructNamespaceString(url, "tl", ns);
        String line = fetch(url.toString(), "getTemplates");

        // xml form: <tl ns="10" title="Template:POTD" />
        List<String> templates = new ArrayList<>(750);
        for (int a = line.indexOf("<tl "); a > 0; a = line.indexOf("<tl ", ++a))
            templates.add(parseAttribute(line, "title", a));

        int size = templates.size();
        log(Level.INFO, "getTemplates", "Successfully retrieved templates used on " + title + " (" + size + " templates)");
        return templates.toArray(new String[size]);
    }

    /**
     *  Gets the list of interwiki links a particular page has. The returned
     *  map has the format { language code : the page on the external wiki
     *  linked to }.
     *
     *  @param title a page
     *  @return a map of interwiki links that page has (empty if there are no
     *  links)
     *  @throws IOException if a network error occurs
     *  @since 0.18
     */
    public Map<String, String> getInterWikiLinks(String title) throws IOException
    {
        String url = query + "prop=langlinks&lllimit=max&titles=" + encode(title, true);
        String line = fetch(url, "getInterwikiLinks");

        // xml form: <ll lang="en" />Main Page</ll> or <ll lang="en" /> for [[Main Page]]
        Map<String, String> interwikis = new HashMap<>(750);
        for (int a = line.indexOf("<ll "); a > 0; a = line.indexOf("<ll ", ++a))
        {
            String language = parseAttribute(line, "lang", a);
            int b = line.indexOf('>', a) + 1;
            int c = line.indexOf('<', b);
            String page = decode(line.substring(b, c));
            interwikis.put(language, page);
        }
        log(Level.INFO, "getInterWikiLinks", "Successfully retrieved interwiki links on " + title);
        return interwikis;
    }

    /**
     *  Gets the list of wikilinks used on a particular page. Patch somewhat by
     *  wim.jongman
     *
     *  @param title a page
     *  @return the list of links used in the page
     *  @throws IOException if a network error occurs
     *  @since 0.24
     */
    public String[] getLinksOnPage(String title) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=links&pllimit=max&titles=");
        url.append(encode(title, true));
        String plcontinue = null;
        List<String> links = new ArrayList<>(750);
        do
        {
            String line;
            if (plcontinue == null)
                line = fetch(url.toString(), "getLinksOnPage");
            else
                line = fetch(url.toString() + "&plcontinue=" + encode(plcontinue, false), "getLinksOnPage");
            plcontinue = parseAttribute(line, "plcontinue", 0);

            // xml form: <pl ns="6" title="page name" />
            for (int a = line.indexOf("<pl "); a > 0; a = line.indexOf("<pl ", ++a))
                links.add(parseAttribute(line, "title", a));
        }
        while (plcontinue != null);

        int size = links.size();
        log(Level.INFO, "getLinksOnPage", "Successfully retrieved links used on " + title + " (" + size + " links)");
        return links.toArray(new String[size]);
    }

    /**
     *  Gets the list of external links used on a particular page.
     *
     *  @param title a page
     *  @return the list of external links used in the page
     *  @throws IOException if a network error occurs
     *  @since 0.29
     */
    public String[] getExternalLinksOnPage(String title) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=extlinks&ellimit=max&titles=");
        url.append(encode(title, true));
        String eloffset = null;
        List<String> links = new ArrayList<>(750);
        do
        {
            String line;
            if (eloffset == null)
                line = fetch(url.toString(), "getExternalLinksOnPage");
            else
                line = fetch(url.toString() + "&eloffset=" + encode(eloffset, false), "getExternalLinksOnPage");
            eloffset = parseAttribute(line, "eloffset", 0);

            // xml form: <pl ns="6" title="page name" />
            for (int a = line.indexOf("<el "); a > 0; a = line.indexOf("<el ", ++a))
            {
                int x = line.indexOf('>', a) + 1;
                int y = line.indexOf("</el>", x);
                links.add(decode(line.substring(x, y)));
            }
        }
        while (eloffset != null);

        int size = links.size();
        log(Level.INFO, "getExternalLinksOnPage", "Successfully retrieved external links used on " + title + " (" + size + " links)");
        return links.toArray(new String[size]);
    }

    /**
     *  Gets the list of sections on a particular page. The returned map pairs
     *  the section numbering as in the table of contents with the section
     *  title, as in the following example:
     *
     *  { 1 : How to nominate }, <br>
     *  { 1.1 : Step 1 - Evaluate }, <br>
     *  { 1.2 : Step 2 - Create subpage }, <br>
     *  { 1.2.1 : Step 2.5 - Transclude and link }, <br>
     *  { 1.3 : Step 3 - Update image }, <br>
     *  ...
     *
     *  @param page the page to get sections for
     *  @return the section map for that page
     *  @throws IOException if a network error occurs
     *  @since 0.18
     */
    public LinkedHashMap<String, String> getSectionMap(String page) throws IOException
    {
        String url = apiUrl + "action=parse&text={{:" + encode(page, true) + "}}__TOC__&prop=sections";
        String line = fetch(url, "getSectionMap");

        // xml form: <s toclevel="1" level="2" line="How to nominate" number="1" />
        LinkedHashMap<String, String> map = new LinkedHashMap<>(30);
        for (int a = line.indexOf("<s "); a > 0; a = line.indexOf("<s ", ++a))
        {
            String title = parseAttribute(line, "line", a);
            String number = parseAttribute(line, "number", a);
            map.put(number, title);
        }
        log(Level.INFO, "getSectionMap", "Successfully retrieved section map for " + page);
        return map;
    }

    /**
     *  Gets the most recent revision of a page, or null if the page does not exist.
     *  @param title a page
     *  @return the most recent revision of that page
     *  @throws IOException if a network error occurs
     *  @since 0.24
     */
    public Revision getTopRevision(String title) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=revisions&rvlimit=1&meta=tokens&type=rollback&titles=");
        url.append(encode(title, true));
        url.append("&rvprop=timestamp%7Cuser%7Cids%7Cflags%7Csize%7Ccomment");
        String line = fetch(url.toString(), "getTopRevision");
        int a = line.indexOf("<rev "); // important space
        int b = line.indexOf("/>", a);
        if (a < 0) // page does not exist
            return null;
        return parseRevision(line.substring(a, b), title);
    }

    /**
     *  Gets the first revision of a page, or null if the page does not exist.
     *  @param title a page
     *  @return the oldest revision of that page
     *  @throws IOException if a network error occurs
     *  @since 0.24
     */
    public Revision getFirstRevision(String title) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("prop=revisions&rvlimit=1&rvdir=newer&titles=");
        url.append(encode(title, true));
        url.append("&rvprop=timestamp%7Cuser%7Cids%7Cflags%7Csize%7Ccomment");
        String line = fetch(url.toString(), "getFirstRevision");
        int a = line.indexOf("<rev "); // important space!
        int b = line.indexOf("/>", a);
        if (a < 0) // page does not exist
            return null;
        return parseRevision(line.substring(a, b), title);
    }

    /**
     *  Gets the newest page name or the name of a page where the asked page
     *  redirects.
     *  @param title a title
     *  @return the page redirected to or null if not a redirect
     *  @throws IOException if a network error occurs
     *  @since 0.29
     */
    public String resolveRedirect(String title) throws IOException
    {
        return resolveRedirects(new String[] { title })[0];
    }

    /**
     *  Gets the newest page name or the name of a page where the asked pages
     *  redirect.
     *  @param titles a list of titles.
     *  @return for each title, the page redirected to or null if not a redirect
     *  @throws IOException if a network error occurs
     *  @since 0.29
     *  @author Nirvanchik/MER-C
     */
    public String[] resolveRedirects(String[] titles) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        if (!resolveredirect)
            url.append("redirects&");
        url.append("titles=");
        String[] ret = new String[titles.length];
        String[] temp = constructTitleString(titles);
        for (String blah : temp)
        {
            String line = fetch(url.toString() + blah, "resolveRedirects");

            // expected form: <redirects><r from="Main page" to="Main Page"/>
            // <r from="Home Page" to="Home page"/>...</redirects>
            // TODO: look for the <r> tag instead
            for (int j = line.indexOf("<r "); j > 0; j = line.indexOf("<r ", ++j))
            {
                String parsedtitle = parseAttribute(line, "from", j);
                for (int i = 0; i < titles.length; i++)
                    if (normalize(titles[i]).equals(parsedtitle))
                        ret[i] = parseAttribute(line, "to", j);
            }
        }
        return ret;
    }

    /**
     *  Gets the entire revision history of a page. Be careful when using
     *  this method as some pages (such as [[Wikipedia:Administrators'
     *  noticeboard/Incidents]] have ~10^6 revisions.
     *
     *  @param title a page
     *  @return the revisions of that page
     *  @throws IOException if a network error occurs
     *  @since 0.19
     */
    public Revision[] getPageHistory(String title) throws IOException
    {
        return getPageHistory(title, null, null, false);
    }

    /**
     *  Gets the revision history of a page between two dates.
     *  @param title a page
     *  @param start the EARLIEST of the two dates
     *  @param end the LATEST of the two dates
     *  @param reverse whether to put the oldest first (default = false, newest
     *  first is how history pages work)
     *  @return the revisions of that page in that time span
     *  @throws IOException if a network error occurs
     *  @since 0.19
     */
    public Revision[] getPageHistory(String title, Calendar start, Calendar end, boolean reverse) throws IOException
    {
        // set up the url
        StringBuilder url = new StringBuilder(query);
        url.append("prop=revisions&rvlimit=max&titles=");
        url.append(encode(title, true));
        url.append("&rvprop=timestamp%7Cuser%7Cids%7Cflags%7Csize%7Ccomment");
        if (reverse)
            url.append("&rvdir=newer");
        if (start != null)
        {
            url.append(reverse ? "&rvstart=" : "&rvend=");
            url.append(calendarToTimestamp(start));
        }
        if (end != null)
        {
            url.append(reverse ? "&rvend=" : "&rvstart=");
            url.append(calendarToTimestamp(end));
        }
        String rvcontinue = null;
        List<Revision> revisions = new ArrayList<>(1500);

        // main loop
        do
        {
            String line;
            if (rvcontinue == null)
                line = fetch(url.toString(), "getPageHistory");
            else
                line = fetch(url.toString() + "&rvcontinue=" + rvcontinue, "getPageHistory");
            rvcontinue = parseAttribute(line, "rvcontinue", 0);

            // parse stuff
            for (int a = line.indexOf("<rev "); a > 0; a = line.indexOf("<rev ", ++a))
            {
                int b = line.indexOf("/>", a);
                revisions.add(parseRevision(line.substring(a, b), title));
            }
        }
        while (rvcontinue != null);
        // populate previous/next
        int size = revisions.size();
        Revision[] temp = revisions.toArray(new Revision[size]);
        for (int i = 0; i < size; i++)
        {
            if (i != 0)
                temp[i].next = temp[i - 1].revid;
            if (i != size - 1)
                temp[i].sizediff = temp[i].size - temp[i + 1].size;
            else
                temp[i].sizediff = temp[i].size;
        }
        log(Level.INFO, "getPageHistory", "Successfully retrieved page history of " + title + " (" + size + " revisions)");
        return temp;
    }

    /**
     *  Gets the deleted history of a page.
     *  @param title a page
     *  @return the deleted revisions of that page in that time span
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot obtain deleted revisions
     *  @since 0.30
     */
    public Revision[] getDeletedHistory(String title) throws IOException, CredentialNotFoundException
    {
        return getDeletedHistory(title, null, null, false);
    }

    /**
     *  Gets the deleted history of a page.
     *  @param title a page
     *  @param start the EARLIEST of the two dates
     *  @param end the LATEST of the two dates
     *  @param reverse whether to put the oldest first (default = false, newest
     *  first is how history pages work)
     *  @return the deleted revisions of that page in that time span
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot obtain deleted revisions
     *  @since 0.30
     */
    public Revision[] getDeletedHistory(String title, Calendar start, Calendar end, boolean reverse)
        throws IOException, CredentialNotFoundException
    {
        // admin queries are annoying
        if (user == null || !user.isAllowedTo("deletedhistory"))
            throw new CredentialNotFoundException("Permission denied: not able to view deleted history");

        StringBuilder url = new StringBuilder(query);
        url.append("prop=deletedrevisions&drvprop=ids%7Cuser%7Cflags%7Csize%7Ccomment&drvlimit=max");
        if (reverse)
            url.append("&drvdir=newer");
        if (start != null)
        {
            url.append(reverse ? "&drvstart=" : "&drvend=");
            url.append(calendarToTimestamp(start));
        }
        if (end != null)
        {
            url.append(reverse ? "&drvend=" : "&drvstart=");
            url.append(calendarToTimestamp(end));
        }
        url.append("&titles=");
        url.append(encode(title, true));

        String drvcontinue = null;
        List<Revision> delrevs = new ArrayList<>(500);
        do
        {
            String response;
            if (drvcontinue != null)
                response = fetch(url.toString() + "&drvcontinue=" + encode(drvcontinue, false), "getDeletedHistory");
            else
                response = fetch(url.toString(), "getDeletedHistory");
            drvcontinue = parseAttribute(response, "drvcontinue", 0);

            // parse
            int x = response.indexOf("<deletedrevs>");
            if (x < 0) // no deleted history
                break;
            for (x = response.indexOf("<page ", x); x > 0; x = response.indexOf("<page ", ++x))
            {
                String deltitle = parseAttribute(response, "title", x);
                int y = response.indexOf("</page>", x);
                for (int z = response.indexOf("<rev ", x); z < y && z >= 0; z = response.indexOf("<rev ", ++z))
                {
                    int aa = response.indexOf(" />", z);
                    Revision temp = parseRevision(response.substring(z, aa), deltitle);
                    temp.pageDeleted = true;
                    delrevs.add(temp);
                }
            }
        }
        while (drvcontinue != null);

        int size = delrevs.size();
        log(Level.INFO, "Successfully fetched " + size + " deleted revisions.", "deletedRevs");
        return delrevs.toArray(new Revision[size]);
    }

    /**
     *  Gets the deleted contributions of a user. Equivalent to
     *  [[Special:Deletedcontributions]].
     *  @param u a user
     *  @return the deleted contributions of that user
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot obtain deleted revisions
     *  @since 0.30
     */
    public Revision[] deletedContribs(String u) throws IOException, CredentialNotFoundException
    {
        return deletedContribs(u, null, null, false, ALL_NAMESPACES);
    }

    /**
     *  Gets the deleted contributions of a user in the given namespace. Equivalent to
     *  [[Special:Deletedcontributions]].
     *  @param username a user
     *  @param start the EARLIEST of the two dates
     *  @param end the LATEST of the two dates
     *  @param reverse whether to put the oldest first (default = false, newest
     *  first is how history pages work)
     *  @param namespace a list of namespaces
     *  @return the deleted contributions of that user
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot obtain deleted revisions
     *  @since 0.30
     */
    public Revision[] deletedContribs(String username, Calendar end, Calendar start, boolean reverse, int... namespace)
        throws IOException, CredentialNotFoundException
    {
        // admin queries are annoying
        if (user == null || !user.isAllowedTo("deletedhistory"))
            throw new CredentialNotFoundException("Permission denied: not able to view deleted history");

        StringBuilder url = new StringBuilder(query);
        url.append("list=alldeletedrevisions&adrprop=ids%7Cuser%7Cflags%7Csize%7Ccomment%7Ctimestamp&adrlimit=max");
        if (reverse)
            url.append("&adrdir=newer");
        if (start != null)
        {
            url.append(reverse ? "&adrstart=" : "&adrend=");
            url.append(calendarToTimestamp(start));
        }
        if (end != null)
        {
            url.append(reverse ? "&adrend=" : "&adrstart=");
            url.append(calendarToTimestamp(end));
        }
        url.append("&adruser=");
        url.append(encode(username, true));
        constructNamespaceString(url, "adr", namespace);

        String adrcontinue = null;
        List<Revision> delrevs = new ArrayList<>(500);
        do
        {
            String response;
            if (adrcontinue != null)
                response = fetch(url.toString() + "&adrcontinue=" + encode(adrcontinue, false), "deletedContribs");
            else
                response = fetch(url.toString(), "deletedContribs");
            adrcontinue = parseAttribute(response, "adrcontinue", 0);

            // parse
            int x = response.indexOf("<alldeletedrevisions>");
            if (x < 0) // no deleted history
                break;
            for (x = response.indexOf("<page ", x); x > 0; x = response.indexOf("<page ", ++x))
            {
                String deltitle = parseAttribute(response, "title", x);
                int y = response.indexOf("</page>", x);
                for (int z = response.indexOf("<rev ", x); z < y && z >= 0; z = response.indexOf("<rev ", ++z))
                {
                    int aa = response.indexOf(" />", z);
                    Revision temp = parseRevision(response.substring(z, aa), deltitle);
                    temp.pageDeleted = true;
                    delrevs.add(temp);
                }
            }
        }
        while (adrcontinue != null);

        int size = delrevs.size();
        log(Level.INFO, "Successfully fetched " + size + " deleted revisions.", "deletedRevs");
        return delrevs.toArray(new Revision[size]);
    }

    /**
     *  Returns all deleted pages that begin with the given prefix. WARNING:
     *  this does not behave like [[Special:Prefixindex]]. See [[Special:Undelete]]
     *  with no arguments.
     *
     *  @param prefix a prefix without a namespace specifier, empty string
     *  lists all deleted pages in the namespace.
     *  @param namespace one (and only one) namespace -- not ALL_NAMESPACES
     *  @return (see above)
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot view deleted pages
     *  @throws IllegalArgumentException if namespace == ALL_NAMESPACES
     *  @since 0.31
     */
    public String[] deletedPrefixIndex(String prefix, int namespace) throws IOException, CredentialNotFoundException
    {
        if (user == null || !user.isAllowedTo("deletedhistory") || !user.isAllowedTo("deletedtext"))
            throw new CredentialNotFoundException("Permission denied: not able to view deleted history or text.");
        
        // disallow ALL_NAMESPACES, this query is extremely slow and likely to error out.
        if (namespace == ALL_NAMESPACES)
            throw new IllegalArgumentException("deletedPrefixIndex: you must choose a namespace.");
        
        StringBuilder url = new StringBuilder(query);
        url.append("generator=alldeletedrevisions&gadrdir=newer&gadrgeneratetitles=1&gadrprefix=");
        url.append(encode(prefix, false));
        url.append("&gadrlimit=max&gadrnamespace=");
        url.append(namespace);

        String drcontinue = null;
        List<String> pages = new ArrayList<>();
        do
        {
            String text;
            if (drcontinue == null)
                text = fetch(url.toString(), "deletedPrefixIndex");
            else
                text = fetch(url.toString() + "&gadrcontinue=" + encode(drcontinue, false), "deletedPrefixIndex");
            drcontinue = parseAttribute(text, "gadrcontinue", 0);

            for (int x = text.indexOf("<page ", 0); x > 0; x = text.indexOf("<page ", ++x))
                pages.add(parseAttribute(text, "title", x));
        }
        while (drcontinue != null);
        int size = pages.size();
        log(Level.INFO, "deletedPrefixIndex", "Successfully retrieved deleted page list (" + size + " items).");
        return pages.toArray(new String[size]);
    }

    /**
     *  Gets the text of a deleted page (it's like getPageText, but for deleted
     *  pages).
     *  @param page a page
     *  @return the deleted text, or null if there is no deleted text to retrieve
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if we cannot obtain deleted revisions
     *  @since 0.30
     */
    public String getDeletedText(String page) throws IOException, CredentialNotFoundException
    {
        if (user == null || !user.isAllowedTo("deletedhistory") || !user.isAllowedTo("deletedtext"))
            throw new CredentialNotFoundException("Permission denied: not able to view deleted history or text.");

        // TODO: this can be multiquery(?)
        StringBuilder url = new StringBuilder(query);
        url.append("prop=deletedrevisions&drvlimit=1&drvprop=content&titles=");
        url.append(encode(page, true));

        // expected form: <rev timestamp="2009-04-05T22:40:35Z" xml:space="preserve">TEXT OF PAGE</rev>
        String line = fetch(url.toString(), "getDeletedText");
        int a = line.indexOf("<rev ");
        if (a < 0)
            return null;
        a = line.indexOf(">", a) + 1;
        int b = line.indexOf("</rev>", a);
        log(Level.INFO, "getDeletedText", "Successfully retrieved deleted text of page " + page);
        return line.substring(a, b);
    }

    /**
     *  Moves a page. Moves the associated talk page and leaves redirects, if
     *  applicable. Equivalent to [[Special:MovePage]]. This method is thread
     *  safe and is subject to the throttle.
     *
     *  @param title the title of the page to move
     *  @param newTitle the new title of the page
     *  @param reason a reason for the move
     *  @throws UnsupportedOperationException if the original page is in the
     *  Category or Image namespace. MediaWiki does not support moving of
     *  these pages.
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialException if page is protected and we can't move it
     *  @since 0.16
     */
    public void move(String title, String newTitle, String reason) throws IOException, LoginException
    {
        move(title, newTitle, reason, false, true, false);
    }

    /**
     *  Moves a page. Equivalent to [[Special:MovePage]]. This method is
     *  thread safe and is subject to the throttle.
     *
     *  @param title the title of the page to move
     *  @param newTitle the new title of the page
     *  @param reason a reason for the move
     *  @param noredirect don't leave a redirect behind. You need to be a
     *  admin to do this, otherwise this option is ignored.
     *  @param movesubpages move the subpages of this page as well. You need to
     *  be an admin to do this, otherwise this will be ignored.
     *  @param movetalk move the talk page as well (if applicable)
     *  @throws UnsupportedOperationException if the original page is in the
     *  Category namespace. MediaWiki does not support moving of these pages.
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialException if page is protected and we can't move it
     *  @since 0.16
     */
    public synchronized void move(String title, String newTitle, String reason, boolean noredirect, boolean movetalk,
        boolean movesubpages) throws IOException, LoginException
    {
        throttle();
        // check for log in
        if (user == null || !user.isAllowedTo("move"))
        {
            CredentialNotFoundException ex = new CredentialNotFoundException("Permission denied: cannot move pages.");
            log(Level.SEVERE, "move", "Cannot move - permission denied: " + ex);
            throw ex;
        }

        // check namespace
        if (namespace(title) == CATEGORY_NAMESPACE)
            throw new UnsupportedOperationException("Tried to move a category.");

        // protection and token
        Map info = getPageInfo(title);
        // determine whether the page exists
        if (!(Boolean)info.get("exists"))
            throw new IllegalArgumentException("Tried to move a non-existant page!");
        if (!checkRights(info, "move"))
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            log(Level.WARNING, "move", "Cannot move - permission denied. " + ex);
            throw ex;
        }

        // post data
        StringBuilder buffer = new StringBuilder(10000);
        buffer.append("from=");
        buffer.append(encode(title, true));
        buffer.append("&to=");
        buffer.append(encode(newTitle, true));
        buffer.append("&reason=");
        buffer.append(encode(reason, false));
        buffer.append("&token=");
        buffer.append(encode(getToken("csrf"), false));
        if (movetalk)
            buffer.append("&movetalk=1");
        if (noredirect && user.isAllowedTo("suppressredirect"))
            buffer.append("&noredirect=1");
        if (movesubpages && user.isAllowedTo("move-subpages"))
            buffer.append("&movesubpages=1");
        String response = post(apiUrl + "action=move", buffer.toString(), "move");

        // done
        try
        {
            // success
            if (!response.contains("move from"))
                checkErrorsAndUpdateStatus(response, "move");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "move", "Exception: " + e.getMessage() + " Retrying...");
                move(title, newTitle, reason, noredirect, movetalk, movesubpages);
            }
            else
            {
                log(Level.SEVERE, "move", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "move", "Successfully moved " + title + " to " + newTitle);
        retry = true;
    }

    /**
     *  Protects a page. Structure of <tt>protectionstate</tt> (everything is
     *  optional, if a value is not present, then the corresponding values will
     *  be left untouched):
     *  <pre>
     *  {
     *     edit: one of { NO_PROTECTION, SEMI_PROTECTION, FULL_PROTECTION }, // restricts editing
     *     editexpiry: Calendar, // expiry time for edit protection, null = indefinite
     *     move, moveexpiry, // as above, prevents page moving
     *     create, createexpiry, // as above, prevents page creation (no effect on existing pages)
     *     upload, uploadexpiry, // as above, prevents uploading of files (FILE_NAMESPACE only)
     *     cascade: Boolean, // Enables cascading protection (requires edit=FULL_PROTECTION). Default: false.
     *     cascadesource: String // souce of cascading protection (here ignored)
     *  };
     *  </pre>
     *
     *  @param page the page
     *  @param protectionstate (see above)
     *  @param reason the reason for (un)protection
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialNotFoundException if we cannot protect
     *  @since 0.30
     */
    public synchronized void protect(String page, Map<String, Object> protectionstate, String reason) throws IOException, LoginException
    {
        throttle();
        
        if (user == null || !user.isAllowedTo("protect"))
            throw new CredentialNotFoundException("Cannot protect: permission denied.");
        
        StringBuilder out = new StringBuilder("title=");
        out.append(encode(page, true));
        out.append("&reason=");
        out.append(encode(reason, false));
        out.append("&token=");
        out.append(encode(getToken("csrf"), false));
        // cascade protection
        if (protectionstate.containsKey("cascade"))
            out.append("&cascade=1");
        // protection levels
        out.append("&protections=");
        StringBuilder temp = new StringBuilder();
        for (Map.Entry<String, Object> entry : protectionstate.entrySet())
        {
            String key = entry.getKey();
            if (!key.contains("expiry") && !key.equals("cascade"))
            {
                out.append(key);
                out.append("=");
                out.append(entry.getValue());
                Calendar expiry = (Calendar)protectionstate.get(key + "expiry");
                temp.append(expiry == null ? "never" : calendarToTimestamp(expiry));
                out.append("%7C");
                temp.append("%7C");
            }
        }
        out.delete(out.length() - 3, out.length());
        temp.delete(temp.length() - 3, temp.length());
        out.append("&expiry=");
        out.append(temp);
        System.out.println(out); // TODO remove

        String response = post(apiUrl + "action=protect", out.toString(), "protect");
        try
        {
            if (!response.contains("<protect "))
                checkErrorsAndUpdateStatus(response, "protect");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "protect", "Exception: " + e.getMessage() + " Retrying...");
                protect(page, protectionstate, reason);
            }
            else
            {
                log(Level.SEVERE, "protect", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "edit", "Successfully protected " + page);
        retry = true;
    }

    /**
     *  Completely unprotects a page.
     *  @param page the page to unprotect
     *  @param reason the reason for unprotection
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialNotFoundException if we cannot protect
     *  @since 0.30
     */
    public void unprotect(String page, String reason) throws IOException, LoginException
    {
        Map<String, Object> state = new HashMap<>();
        state.put("edit", NO_PROTECTION);
        state.put("move", NO_PROTECTION);
        if (namespace(page) == FILE_NAMESPACE)
            state.put("upload", NO_PROTECTION);
        state.put("create", NO_PROTECTION);
        protect(page, state, reason);
    }

    /**
     *  Exports the current revision of this page. Equivalent to
     *  [[Special:Export]].
     *  @param title the title of the page to export
     *  @return the exported text
     *  @throws IOException if a network error occurs
     *  @since 0.20
     */
    public String export(String title) throws IOException
    {
        return fetch(query + "export&exportnowrap&titles=" + encode(title, true), "export");
    }

    // REVISION METHODS

    /**
     *  Gets a revision based on a given oldid. Automatically fills out all
     *  attributes of that revision except <tt>rcid</tt> and <tt>rollbacktoken</tt>.
     *
     *  @param oldid an oldid
     *  @return the revision corresponding to that oldid, or null if it has been
     *  deleted
     *  @throws IOException if a network error occurs
     *  @since 0.17
     */
    public Revision getRevision(long oldid) throws IOException
    {
        return getRevisions(new long[] { oldid })[0];
    }

    /**
     *  Gets revisions based on given oldids. Automatically fills out all
     *  attributes of those revisions except <tt>rcid</tt> and <tt>rollbacktoken</tt>.
     *
     *  @param oldids a list of oldids
     *  @return the revisions corresponding to those oldids, in the order of the
     *  input array. If a particular revision has been deleted, the corresponding
     *  index is null.
     *  @throws IOException if a network error occurs
     *  @since 0.29
     */
    public Revision[] getRevisions(long[] oldids) throws IOException
    {
        // build url and connect
        StringBuilder url = new StringBuilder(query);
        url.append("prop=revisions&rvprop=ids%7Ctimestamp%7Cuser%7Ccomment%7Cflags%7Csize&revids=");
        // chunkify oldids
        String[] chunks = new String[oldids.length / slowmax + 1];
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < oldids.length; i++)
        {
            buffer.append(oldids[i]);
            if (i == oldids.length - 1 || i == slowmax - 1)
            {
                chunks[i / slowmax] = buffer.toString();
                buffer = new StringBuilder();
            }
            else
                buffer.append("%7C");
        }
        Revision[] revisions = new Revision[oldids.length];
        // fetch and parse
        for (String chunk : chunks)
        {
            String line = fetch(url.toString() + chunk, "getRevision");

            for (int i = line.indexOf("<page "); i > 0; i = line.indexOf("<page ", ++i))
            {
                int z = line.indexOf("</page>", i);
                String title = parseAttribute(line, "title", i);
                for (int j = line.indexOf("<rev ", i); j > 0 && j < z; j = line.indexOf("<rev ", ++j))
                {
                    int y = line.indexOf("/>", j);
                    String blah = line.substring(j, y);
                    Revision rev = parseRevision(blah, title);
                    long oldid = rev.getRevid();
                    for (int k = 0; k < oldids.length; k++)
                        if (oldids[k] == oldid)
                            revisions[k] = rev;
                }
            }
        }
        return revisions;
    }

    /**
     *  Reverts a series of edits on the same page by the same user quickly
     *  provided that they are the most recent revisions on that page. If this
     *  is not the case, then this method does nothing. See
     *  [[mw:Manual:Parameters to index.php#Actions]] (look under rollback)
     *  for more information. The edit and reverted edits will be marked as bot
     *  if <tt>isMarkBot() == true</tt>.
     *
     *  @param revision the revision to revert. <tt>revision.isTop()</tt> must
     *  be true for the rollback to succeed
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if the user is not an admin
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws AccountLockedException if the user is blocked
     *  @since 0.19
     */
    public void rollback(Revision revision) throws IOException, LoginException
    {
        rollback(revision, markbot, "");
    }

    /**
     *  Reverts a series of edits on the same page by the same user quickly
     *  provided that they are the most recent revisions on that page. If this
     *  is not the case, then this method does nothing. See
     *  [[mw:Manual:Parameters to index.php#Actions]] (look under rollback)
     *  for more information.
     *
     *  @param revision the revision to revert. <tt>revision.isTop()</tt> must
     *  be true for the rollback to succeed
     *  @param bot whether to mark this edit and the reverted revisions as
     *  bot edits (ignored if we cannot do this)
     *  @param reason (optional) a reason for the rollback. Use "" for the
     *  default ([[MediaWiki:Revertpage]]).
     *  @throws IOException if a network error occurs
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialNotFoundException if the user cannot rollback
     *  @throws AccountLockedException if the user is blocked
     *  @since 0.19
     */
    public synchronized void rollback(Revision revision, boolean bot, String reason) throws IOException, LoginException
    {
        // check rights
        if (user == null || !user.isAllowedTo("rollback"))
            throw new CredentialNotFoundException("Permission denied: cannot rollback.");

        // check whether we are "on top".
        Revision top = getTopRevision(revision.getPage());
        if (!top.equals(revision))
        {
            log(Level.INFO, "rollback", "Rollback failed: revision is not the most recent");
            return;
        }

        // get the rollback token
        String token = encode(top.getRollbackToken(), false);

        // Perform the rollback. Although it's easier through the human interface, we want
        // to make sense of any resulting errors.
        StringBuilder buffer = new StringBuilder(10000);
        buffer.append("title=");
        buffer.append(encode(revision.getPage(), true));
        buffer.append("&user=");
        buffer.append(encode(revision.getUser(), true));
        buffer.append("&token=");
        buffer.append(token);
        if (bot && user.isAllowedTo("markbotedits"))
            buffer.append("&markbot=1");
        if (!reason.isEmpty())
        {
            buffer.append("&summary=");
            buffer.append(reason);
        }
        String response = post(apiUrl + "action=rollback", buffer.toString(), "rollback");

        // done
        try
        {
            // ignorable errors
            if (response.contains("alreadyrolled"))
                log(Level.INFO, "rollback", "Edit has already been rolled back.");
            else if (response.contains("onlyauthor"))
                log(Level.INFO, "rollback", "Cannot rollback as the page only has one author.");
            // probably not ignorable (otherwise success)
            else if (!response.contains("rollback title="))
                checkErrorsAndUpdateStatus(response, "rollback");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "rollback", "Exception: " + e.getMessage() + " Retrying...");
                rollback(revision, bot, reason);
            }
            else
            {
                log(Level.SEVERE, "rollback", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "rollback", "Successfully reverted edits by " + user + " on " + revision.getPage());
        retry = true;
    }

    /**
     *  Deletes and undeletes revisions.
     *
     *  @param hidecontent hide the content of the revision (true/false
     *  = hide/unhide, null = status quo)
     *  @param hidereason hide the edit summary or the reason for an action
     *  @param hideuser hide who made the revision/action
     *  @param reason the reason why the (un)deletion was performed
     *  @param suppress [[Wikipedia:Oversight]] the information in question
     *  (ignored if we cannot <tt>suppressrevision</tt>, null = status quo).
     *  @param revisions the list of revisions to (un)delete
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if you do not have the rights to
     *  delete revisions or log entries
     *  @throws AccountLockedException if the user is blocked
     */
    public synchronized void revisionDelete(Boolean hidecontent, Boolean hideuser, Boolean hidereason, String reason, Boolean suppress,
        Revision[] revisions) throws IOException, LoginException
    {
        throttle();

        if (user == null || !user.isAllowedTo("deleterevision") || !user.isAllowedTo("deletelogentry"))
            throw new CredentialNotFoundException("Permission denied: cannot revision delete.");

        StringBuilder out = new StringBuilder("reason=");
        out.append(encode(reason, false));
        out.append("&type=revision"); // FIXME: allow log entry deletion
        out.append("&ids=");
        for (int i = 0; i < revisions.length - 1; i++) // FIXME: allow more than slowmax revisions
        {
            out.append(revisions[i].getRevid());
            out.append("%7C");
        }
        out.append(revisions[revisions.length - 1].getRevid());
        out.append("&token=");
        out.append(encode(getToken("csrf"), false));
        if (user.isAllowedTo("suppressrevision") && suppress != null)
        {
            if (suppress)
                out.append("&suppress=yes");
            else
                out.append("&suppress=no");
        }
        // this is really stupid... I'm open to suggestions
        out.append("&hide=");
        StringBuilder temp = new StringBuilder("&show=");
        if (hidecontent == Boolean.TRUE)
            out.append("content%7C");
        else if (hidecontent == Boolean.FALSE)
            temp.append("content%7C");
        if (hideuser == Boolean.TRUE)
            out.append("user%7C");
        else if (hideuser == Boolean.FALSE)
            temp.append("user%7C");
        if (hidereason == Boolean.TRUE)
            out.append("comment");
        else if (hidereason == Boolean.FALSE)
            temp.append("comment");
        if (out.lastIndexOf("%7C") == out.length() - 2)
            out.delete(out.length() - 2, out.length());
        if (temp.lastIndexOf("%7C") == temp.length() - 2)
            temp.delete(temp.length() - 2, temp.length());
        out.append(temp);

        // send/read response
        String response = post(apiUrl + "action=revisiondelete", out.toString(), "revisionDelete");
        try
        {
            // success
            if (!response.contains("<revisiondelete "))
                checkErrorsAndUpdateStatus(response, "move");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "revisionDelete", "Exception: " + e.getMessage() + " Retrying...");
                revisionDelete(hidecontent, hideuser, hidereason, reason, suppress, revisions);
            }
            else
            {
                log(Level.SEVERE, "revisionDelete", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "revisionDelete", "Successfully (un)deleted " + revisions.length + " revisions.");
        retry = true;
        for (Revision rev : revisions)
        {
            if (hideuser != null)
                rev.userDeleted = hideuser;
            if (hidereason != null)
                rev.summaryDeleted = hidereason;
        }
    }

    /**
     *  Undoes revisions, equivalent to the "undo" button in the GUI page
     *  history. A quick explanation on how this might work - suppose the edit
     *  history was as follows:
     *
     *  <ul>
     *  <li> (revid=541) 2009-01-13 00:01 92.45.43.227
     *  <li> (revid=325) 2008-12-10 11:34 Example user
     *  <li> (revid=314) 2008-12-10 10:15 127.0.0.1
     *  <li> (revid=236) 2008-08-08 08:00 Anonymous
     *  <li> (revid=200) 2008-07-31 16:46 EvilCabalMember
     *  </ul>
     *  Then:
     *  <pre>
     *  wiki.undo(wiki.getRevision(314L), null, reason, false); // undo revision 314 only
     *  wiki.undo(wiki.getRevision(236L), wiki.getRevision(325L), reason, false); // undo revisions 236-325
     *  </pre>
     *
     *  This will only work if revision 541 or any subsequent edits do not
     *  clash with the change resulting from the undo.
     *
     *  @param rev a revision to undo
     *  @param to the most recent in a range of revisions to undo. Set to null
     *  to undo only one revision.
     *  @param reason an edit summary (optional). Use "" to get the default
     *  [[MediaWiki:Undo-summary]].
     *  @param minor whether this is a minor edit
     *  @param bot whether this is a bot edit
     *  @throws IOException if a network error occurs
     *  @throws AccountLockedException if user is blocked
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws CredentialException if page is protected and we can't edit it
     *  @throws IllegalArgumentException if the revisions are not on the same
     *  page.
     *  @since 0.20
     */
    public synchronized void undo(Revision rev, Revision to, String reason, boolean minor,
        boolean bot) throws IOException, LoginException
    {
        throttle();

        // check here to see whether the titles correspond
        if (to != null && !rev.getPage().equals(to.getPage()))
            throw new IllegalArgumentException("Cannot undo - the revisions supplied are not on the same page!");

        // protection
        Map info = getPageInfo(rev.getPage());
        if (!checkRights(info, "edit"))
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            log(Level.WARNING, "undo", "Cannot edit - permission denied." + ex);
            throw ex;
        }

        // send data
        StringBuilder buffer = new StringBuilder(10000);
        buffer.append("title=");
        buffer.append(rev.getPage());
        if (!reason.isEmpty())
        {
            buffer.append("&summary=");
            buffer.append(reason);
        }
        buffer.append("&undo=");
        buffer.append(rev.getRevid());
        if (to != null)
        {
            buffer.append("&undoafter=");
            buffer.append(to.getRevid());
        }
        if (minor)
            buffer.append("&minor=1");
        if (bot)
            buffer.append("&bot=1");
        buffer.append("&token=");
        buffer.append(encode(getToken("csrf"), false));
        String response = post(apiUrl + "action=edit", buffer.toString(), "undo");

        // done
        try
        {
            checkErrorsAndUpdateStatus(response, "undo");
        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "undo", "Exception: " + e.getMessage() + " Retrying...");
                undo(rev, to, reason, minor, bot);
            }
            else
            {
                log(Level.SEVERE, "undo", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
        {
            String log = "Successfully undid revision(s) " + rev.getRevid();
            if (to != null)
                log += (" - " + to.getRevid());
            log(Level.INFO, "undo", log);
        }
        retry = true;
    }

    /**
     *  Parses stuff of the form <tt>title="L. Sprague de Camp"
     *  timestamp="2006-08-28T23:48:08Z" minor="" comment="robot  Modifying:
     *  [[bg:Blah]]"</tt> into useful revision objects. Used by
     *  <tt>contribs()</tt>, <tt>watchlist()</tt>, <tt>getPageHistory()</tt>
     *  <tt>rangeContribs()</tt> and <tt>recentChanges()</tt>. NOTE: if
     *  RevisionDelete was used on a revision, the relevant values will be null.
     *
     *  @param xml the XML to parse
     *  @param title an optional title parameter if we already know what it is
     *  (use "" if we don't)
     *  @return the Revision encoded in the XML
     *  @since 0.17
     */
    protected Revision parseRevision(String xml, String title)
    {
        long oldid = Long.parseLong(parseAttribute(xml, " revid", 0));
        Calendar timestamp = timestampToCalendar(parseAttribute(xml, "timestamp", 0), true);

        // title
        if (title.isEmpty())
            title = parseAttribute(xml, "title", 0);

        // summary
        String summary = null;
        if (xml.contains("comment=\""))
            summary = parseAttribute(xml, "comment", 0);

        // user
        String user2 = null;
        if (xml.contains("user=\""))
            user2 = parseAttribute(xml, "user", 0);

        // flags: minor, bot, new
        boolean minor = xml.contains("minor=\"\"");
        boolean bot = xml.contains("bot=\"\"");
        boolean rvnew = xml.contains("new=\"\"");

        // size
        int size = 0;
        if (xml.contains("newlen=")) // recentchanges
            size = Integer.parseInt(parseAttribute(xml, "newlen", 0));
        else if (xml.contains("size=\""))
            size = Integer.parseInt(parseAttribute(xml, "size", 0));
        else if (xml.contains("len=\"")) // deletedrevs
            size = Integer.parseInt(parseAttribute(xml, "len", 0));

        Revision revision = new Revision(oldid, timestamp, title, summary, user2, minor, bot, rvnew, size);
        // set rcid
        if (xml.contains("rcid=\""))
            revision.setRcid(Long.parseLong(parseAttribute(xml, "rcid", 0)));

        // rollback token; will automatically be null if we cannot rollback
        if (xml.contains("rollbacktoken=\""))
            revision.setRollbackToken(parseAttribute(xml, "rollbacktoken", 0));

        // previous revision
        if (xml.contains("parentid")) // page history/getRevision
            revision.previous = Long.parseLong(parseAttribute(xml, "parentid", 0));
        else if (xml.contains("old_revid")) // watchlist
            revision.previous = Long.parseLong(parseAttribute(xml, "old_revid", 0));

        // sizediff
        if (xml.contains("oldlen=\"")) // recentchanges
            revision.sizediff = revision.size - Integer.parseInt(parseAttribute(xml, "oldlen", 0));
        else if (xml.contains("sizediff=\""))
            revision.sizediff = Integer.parseInt(parseAttribute(xml, "sizediff", 0));

        // revisiondelete
        revision.summaryDeleted = xml.contains("commenthidden=\"");
        revision.userDeleted = xml.contains("userhidden=\"");
        return revision;
    }

    // IMAGE METHODS

    /**
     *  Fetches an image file and returns the image data in a <tt>byte[]</tt>.
     *  Works for external repositories.
     *
     *  @param title the title of the image (may contain "File")
     *  @return the image data or null if the image doesn't exist
     *  @deprecated expects a file as additional parameter
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    @Deprecated
    public byte[] getImage(String title) throws IOException
    {
        return getImage(title, -1, -1);
    }

    /**
     *  Fetches an image and saves it in the given file. Warning: This does overwrite any file content!
     *  Works for external repositories.
     *
     *  @param title the title of the image (may contain "File")
     *  @param file the file to save the image to.
     *  @return true or false if the image doesn't exist
     *  @throws FileNotFoundException if the file is a directory, cannot be created or opened
     *  @throws IOException if a network error occurs
     *  @since 0.30
     */
    public boolean getImage(String title, File file) throws FileNotFoundException, IOException
    {
        return getImage(title, -1, -1, file);
    }

    /**
     *  Fetches a thumbnail of an image file and returns the image data
     *  in a <tt>byte[]</tt>. Works for external repositories.
     *
     *  @param title the title of the image (may contain "File")
     *  @param width the width of the thumbnail (use -1 for actual width)
     *  @param height the height of the thumbnail (use -1 for actual height)
     *  @return the image data or null if the image doesn't exist
     *  @throws IOException if a network error occurs
     *  @deprecated expects a file as additional parameter
     *  @since 0.13
     */
    @Deprecated
    public byte[] getImage(String title, int width, int height) throws IOException
    {
        File image = File.createTempFile("wiki-java_getImage", null);
        image.deleteOnExit();

        boolean downloaded = getImage(title, width, height, image);
        if (!downloaded)
            return null;

        return Files.readAllBytes(image.toPath());
    }

    /**
     *  Fetches a thumbnail of an image file and saves the image data
     *  into the given file. Warning: This does overwrite any file content!
     *  Works for external repositories.
     *
     *  @param title the title of the image (may contain "File")
     *  @param width the width of the thumbnail (use -1 for actual width)
     *  @param height the height of the thumbnail (use -1 for actual height)
     *  @param file a write-able file to save the data to.
     *  @return true or false if the image doesn't exist
     *  @throws FileNotFoundException if the file is a directory, cannot be created or opened
     *  @throws IOException if a network error occurs
     *  @since 0.30
     */
    public boolean getImage(String title, int width, int height, File file) throws FileNotFoundException, IOException
    {
        // this is a two step process - first we fetch the image url
        title = title.replaceFirst("^(File|Image|" + namespaceIdentifier(FILE_NAMESPACE) + "):", "");
        StringBuilder url = new StringBuilder(query);
        url.append("prop=imageinfo&iiprop=url&titles=");
        url.append(encode("File:" + title, true));
        url.append("&iiurlwidth=");
        url.append(width);
        url.append("&iiurlheight=");
        url.append(height);
        String line = fetch(url.toString(), "getImage");
        if (!line.contains("<imageinfo>"))
            return false;
        String url2 = parseAttribute(line, "url", 0);

        // then we read the image
        logurl(url2, "getImage");
        URLConnection connection = makeConnection(url2);
        setCookies(connection);
        connection.connect();

        // there should be a better way to do this
        InputStream input = connection.getInputStream();
        if ("gzip".equals(connection.getContentEncoding()))
            input = new GZIPInputStream(input);

        try (BufferedInputStream in = new BufferedInputStream(input);
            BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file)))
        {
            int c;
            while ((c = in.read()) != -1)
                outStream.write(c);
            outStream.flush();
        }
        log(Level.INFO, "getImage", "Successfully retrieved image \"" + title + "\"");
        return true;
    }

    /**
     *  Gets the file metadata for a file. Note that <tt>getImage()</tt>
     *  reads directly into a <tt>BufferedImage</tt> object, so you won't be
     *  able to get all metadata that way. The keys are:
     *
     *  * size (file size, Integer)
     *  * width (Integer)
     *  * height (Integer)
     *  * sha1 (String)
     *  * mime (MIME type, String)
     *  * plus EXIF metadata (Strings)
     *
     *  @param file the image to get metadata for (may contain "File")
     *  @return the metadata for the image or null if it doesn't exist
     *  @throws IOException if a network error occurs
     *  @since 0.20
     */
    public Map<String, Object> getFileMetadata(String file) throws IOException
    {
        // This seems a good candidate for bulk queries.
        // TODO: support prop=videoinfo
        // fetch
        file = file.replaceFirst("^(File|Image|" + namespaceIdentifier(FILE_NAMESPACE) + "):", "");
        String url = query + "prop=imageinfo&iiprop=size%7Csha1%7Cmime%7Cmetadata&titles="
            + encode("File:" + file, true);
        String line = fetch(url, "getFileMetadata");
        if (line.contains("missing=\"\""))
            return null;
        Map<String, Object> metadata = new HashMap<>(30);

        // size, width, height, sha, mime type
        metadata.put("size", new Integer(parseAttribute(line, "size", 0)));
        metadata.put("width", new Integer(parseAttribute(line, "width", 0)));
        metadata.put("height", new Integer(parseAttribute(line, "height", 0)));
        metadata.put("sha1", parseAttribute(line, "sha1", 0));
        metadata.put("mime", parseAttribute(line, "mime", 0));

        // exif
        while (line.contains("metadata name=\""))
        {
            // TODO: remove this
            int a = line.indexOf("name=\"") + 6;
            int b = line.indexOf('\"', a);
            String name = parseAttribute(line, "name", 0);
            String value = parseAttribute(line, "value", 0);
            metadata.put(name, value);
            line = line.substring(b);
        }
        return metadata;
    }

    /**
     *  Gets duplicates of this file. Capped at <tt>max</tt> number of
     *  duplicates, there's no good reason why there should be more than that.
     *  Equivalent to [[Special:FileDuplicateSearch]].
     *
     *  @param file the file for checking duplicates (may contain "File")
     *  @return the duplicates of that file
     *  @throws IOException if a network error occurs
     *  @since 0.18
     */
    public String[] getDuplicates(String file) throws IOException
    {
        file = file.replaceFirst("^(File|Image|" + namespaceIdentifier(FILE_NAMESPACE) + "):", "");
        String url = query + "prop=duplicatefiles&dflimit=max&titles=" + encode("File:" + file, true);
        String line = fetch(url, "getDuplicates");
        if (line.contains("missing=\"\""))
            return new String[0];

        // xml form: <df name="Star-spangled_banner_002.ogg" other stuff >
        List<String> duplicates = new ArrayList<>(10);
        for (int a = line.indexOf("<df "); a > 0; a = line.indexOf("<df ", ++a))
            duplicates.add("File:" + parseAttribute(line, "name", a));

        int size = duplicates.size();
        log(Level.INFO, "getDuplicates", "Successfully retrieved duplicates of File:" + file + " (" + size + " files)");
        return duplicates.toArray(new String[size]);
    }

    /**
     *  Returns the upload history of an image. This is not the same as
     *  <tt>getLogEntries(null, null, Integer.MAX_VALUE, Wiki.UPLOAD_LOG,
     *  title, Wiki.FILE_NAMESPACE)</tt>, as the image may have been deleted.
     *  This returns only the live history of an image.
     *
     *  @param title the title of the image (may contain File)
     *  @return the image history of the image
     *  @throws IOException if a network error occurs
     *  @since 0.20
     */
    public LogEntry[] getImageHistory(String title) throws IOException
    {
        title = title.replaceFirst("^(File|Image|" + namespaceIdentifier(FILE_NAMESPACE) + "):", "");
        String url = query + "prop=imageinfo&iiprop=timestamp%7Cuser%7Ccomment&iilimit=max&titles="
            + encode("File:" + title, true);
        String line = fetch(url, "getImageHistory");
        if (line.contains("missing=\"\""))
            return new LogEntry[0];
        List<LogEntry> history = new ArrayList<>(40);
        String prefixtitle = namespaceIdentifier(FILE_NAMESPACE) + ":" + title;
        // xml form: <ii timestamp="2010-05-23T05:48:43Z" user="Prodego" comment="Match to new version" />
        for (int a = line.indexOf("<ii "); a > 0; a = line.indexOf("<ii ", ++a))
        {
            int b = line.indexOf('>', a);
            LogEntry le = parseLogEntry(line.substring(a, b));
            le.target = prefixtitle;
            le.type = UPLOAD_LOG;
            le.action = "overwrite";
            history.add(le);
        }

        // crude hack: action adjusting for first image (in the history, not our list)
        int size = history.size();
        LogEntry last = history.get(size - 1);
        last.action = "upload";
        history.set(size - 1, last);
        return history.toArray(new LogEntry[size]);
    }

    /**
     *  Gets an old image revision and writes the image data in a file.
     *  Warning: This does overwrite any file content!
     *  You will have to do the thumbnailing yourself.
     *  @param entry the upload log entry that corresponds to the image being
     *  uploaded
     *  @param file the file to write the image to
     *  @return true if the file exists in the local repository (i.e. not on
     *  Commons or deleted)
     *  @throws FileNotFoundException if the file is a directory, cannot be created or opened
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if the entry is not in the upload log
     *  @since 0.30
     */
    public boolean getOldImage(LogEntry entry, File file) throws IOException
    {
        // check for type
        if (!entry.getType().equals(UPLOAD_LOG))
            throw new IllegalArgumentException("You must provide an upload log entry!");
        // no thumbnails for image history, sorry.
        String title = entry.getTarget();
        String url = query + "prop=imageinfo&iilimit=max&iiprop=timestamp%7Curl%7Carchivename&titles=" + encode(title, true);
        String line = fetch(url, "getOldImage");

        // find the correct log entry by comparing timestamps
        // xml form: <ii timestamp="2010-05-23T05:48:43Z" user="Prodego" comment="Match to new version" />
        for (int a = line.indexOf("<ii "); a > 0; a = line.indexOf("<ii ", ++a))
        {
            String timestamp = convertTimestamp(parseAttribute(line, "timestamp", a));
            if (timestamp.equals(calendarToTimestamp(entry.getTimestamp())))
            {
                // this is it
                url = parseAttribute(line, "url", a);
                logurl(url, "getOldImage");
                URLConnection connection = makeConnection(url);
                setCookies(connection);
                connection.connect();

                // there should be a better way to do this
                InputStream input = connection.getInputStream();
                if ("gzip".equals(connection.getContentEncoding()))
                    input = new GZIPInputStream(input);

                try (BufferedInputStream in = new BufferedInputStream(input);
                    BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(file)))
                {
                    int c;
                    while ((c = in.read()) != -1)
                        outStream.write(c);
                    outStream.flush();

                    // scrape archive name for logging purposes
                    String archive = parseAttribute(line, "archivename", 0);
                    if (archive == null)
                        archive = title;
                    log(Level.INFO, "getOldImage", "Successfully retrieved old image \"" + archive + "\"");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  Gets an old image revision and returns the image data in a <tt>byte[]</tt>.
     *  You will have to do the thumbnailing yourself.n
     *  @param entry the upload log entry that corresponds to the image being
     *  uploaded
     *  @return the image data that was uploaded, as long as it exists in the
     *  local repository (i.e. not on Commons or deleted)
     *  @deprecated expects a file as additional parameter
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if the entry is not in the upload log
     *  @since 0.20
     */
    @Deprecated
    public byte[] getOldImage(LogEntry entry) throws IOException
    {
        // @revised 0.24 BufferedImage => byte[]
        File image = File.createTempFile("wiki-java_getOldImage", null);
        image.deleteOnExit();

        boolean downloaded = getOldImage(entry, image);
        if (!downloaded)
            return null;

        return Files.readAllBytes(image.toPath());
    }

    /**
     *  Gets the uploads of a user.
     *  @param user the user to get uploads for
     *  @return a list of all live images the user has uploaded
     *  @throws IOException if a network error occurs
     *  @since 0.28
     */
    public LogEntry[] getUploads(User user) throws IOException
    {
        return getUploads(user, null, null);
    }

    /**
     *  Gets the uploads of a user between the specified times.
     *  @param user the user to get uploads for
     *  @param start the date to start enumeration (use null to not specify one)
     *  @param end the date to end enumeration (use null to not specify one)
     *  @return a list of all live images the user has uploaded
     *  @throws IOException if a network error occurs
     */
    public LogEntry[] getUploads(User user, Calendar start, Calendar end) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("list=allimages&ailimit=max&aisort=timestamp&aiprop=timestamp%7Ccomment&aiuser=");
        url.append(encode(user.getUsername(), false));
        if (start != null)
        {
            url.append("&aistart=");
            url.append(calendarToTimestamp(start));
        }
        if (end != null)
        {
            url.append("&aiend=");
            url.append(calendarToTimestamp(end));
        }
        List<LogEntry> uploads = new ArrayList<>();
        String aicontinue = null;
        do
        {
            String line;
            if (aicontinue == null)
                line = fetch(url.toString(), "getUploads");
            else
                line = fetch(url.toString() + "&aicontinue=" + aicontinue, "getUploads");
            aicontinue = parseAttribute(line, "aicontinue", 0);

            for (int i = line.indexOf("<img "); i > 0; i = line.indexOf("<img ", ++i))
            {
                int b = line.indexOf("/>", i);
                LogEntry le = parseLogEntry(line.substring(i, b));
                le.type = UPLOAD_LOG;
                le.action = "upload"; // unless it's an overwrite?
                le.user = user;
                uploads.add(le);
            }
        }
        while (aicontinue != null);

        int size = uploads.size();
        log(Level.INFO, "getUploads", "Successfully retrieved uploads of " + user.getUsername() + " (" + size + " uploads)");
        return uploads.toArray(new LogEntry[size]);
    }

    /**
     *  Uploads an image. Equivalent to [[Special:Upload]]. Supported
     *  extensions are (case-insensitive) "png", "jpg", "gif" and "svg". You
     *  need to be logged on to do this. Automatically breaks uploads into
     *  2^<tt>LOG2_CHUNK_SIZE</tt> byte size chunks. This method is thread safe
     *  and subject to the throttle.
     *
     *  @param file the image file
     *  @param filename the target file name (may contain File)
     *  @param contents the contents of the image description page, set to ""
     *  if overwriting an existing file
     *  @param reason an upload summary (defaults to <tt>contents</tt>, use ""
     *  to not specify one)
     *  @throws CredentialNotFoundException if not logged in
     *  @throws CredentialException if (page is protected OR file is on a central
     *  repository) and we can't upload
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws IOException if a network/local filesystem error occurs
     *  @throws AccountLockedException if user is blocked
     *  @since 0.21
     */
    public synchronized void upload(File file, String filename, String contents, String reason) throws IOException, LoginException
    {
        // TODO: upload via URL

        // the usual stuff
        throttle();

        // check for log in
        if (user == null || !user.isAllowedTo("upload"))
        {
            CredentialNotFoundException ex = new CredentialNotFoundException("Permission denied: cannot upload files.");
            log(Level.SEVERE, "upload", "Cannot upload - permission denied." + ex);
            throw ex;
        }
        filename = filename.replaceFirst("^(File|Image|" + namespaceIdentifier(FILE_NAMESPACE) + "):", "");

        // protection
        Map info = getPageInfo("File:" + filename);
        if (!checkRights(info, "upload"))
        {
            CredentialException ex = new CredentialException("Permission denied: page is protected.");
            log(Level.WARNING, "upload", "Cannot upload - permission denied." + ex);
            throw ex;
        }

        // chunked upload setup
        long filesize = file.length();
        long chunks = (filesize >> LOG2_CHUNK_SIZE) + 1;
        String filekey = "";
        try (FileInputStream fi = new FileInputStream(file))
        {
            // upload the image
            for (int i = 0; i < chunks; i++)
            {
                Map<String, Object> params = new HashMap<>(50);
                params.put("filename", filename);
                params.put("token", getToken("csrf"));
                params.put("ignorewarnings", "true");
                if (chunks == 1)
                {
                    // Chunks disabled due to a small filesize.
                    // This is just a normal upload.
                    params.put("text", contents);
                    if (!reason.isEmpty())
                        params.put("comment", reason);
                    byte[] by = new byte[fi.available()];
                    fi.read(by);
                    // Why this is necessary?
                    params.put("file\"; filename=\"" + file.getName(), by);
                }
                else
                {
                    long offset = i << LOG2_CHUNK_SIZE;
                    params.put("stash", "1");
                    params.put("offset", "" + offset);
                    params.put("filesize", "" + filesize);
                    if (i != 0)
                        params.put("filekey", filekey);

                    // write the actual file
                    long buffersize = Math.min(1 << LOG2_CHUNK_SIZE, filesize - offset);
                    byte[] by = new byte[(int)buffersize]; // 32 bit problem. Why must array indices be ints?
                    fi.read(by);
                    params.put("chunk\"; filename=\"" + file.getName(), by);
                }

                // done
                String response = multipartPost(apiUrl + "action=upload", params, "upload");
                try
                {
                    // look for filekey
                    if (chunks > 1)
                    {
                        if (response.contains("filekey=\""))
                        {
                            filekey = parseAttribute(response, "filekey", 0);
                            continue;
                        }
                        else
                            throw new IOException("No filekey present! Server response was " + response);
                    }
                    // TODO: check for more specific errors here
                    if (response.contains("error code=\"fileexists-shared-forbidden\""))
                    {
                        CredentialException ex = new CredentialException("Cannot overwrite file hosted on central repository.");
                        log(Level.WARNING, "upload", "Cannot upload - permission denied." + ex);
                        throw ex;
                    }
                    checkErrorsAndUpdateStatus(response, "upload");
                }
                catch (IOException e)
                {
                    // don't bother retrying - uploading is a pain
                    log(Level.SEVERE, "upload", "EXCEPTION: " + e);
                    throw e;
                }
            }
        }

        // unstash upload if chunked
        if (chunks > 1)
        {
            Map<String, Object> params = new HashMap<>(50);
            params.put("filename", filename);
            params.put("token", getToken("csrf"));
            params.put("text", contents);
            if (!reason.isEmpty())
                params.put("comment", reason);
            params.put("ignorewarnings", "true");
            params.put("filekey", filekey);
            String response = multipartPost(apiUrl + "action=upload", params, "upload");
            checkErrorsAndUpdateStatus(response, "upload");
        }
        log(Level.INFO, "upload", "Successfully uploaded to File:" + filename + ".");
    }

    // USER METHODS

    /**
     *  Determines whether a specific user exists. Should evaluate to false
     *  for anons.
     *
     *  @param username a username
     *  @return whether the user exists
     *  @throws IOException if a network error occurs
     *  @since 0.05
     */
    public boolean userExists(String username) throws IOException
    {
        username = encode(username, true);
        return fetch(query + "list=users&ususers=" + username, "userExists").contains("userid=\"");
    }

    /**
     *  Gets the specified number of users (as a String) starting at the
     *  given string, in alphabetical order. Equivalent to [[Special:Listusers]].
     *
     *  @param start the string to start enumeration
     *  @param number the number of users to return
     *  @return a String[] containing the usernames
     *  @throws IOException if a network error occurs
     *  @since 0.05
     */
    public String[] allUsers(String start, int number) throws IOException
    {
        return allUsers(start, number, "", "", "", "");
    }

    /**
     *  Returns all usernames of users in given group(s)
     *  @param group a group name. Use pipe-char "|" to separate if several 
     *  @return (see above)
     *  @throws IOException if a network error occurs
     *  @since 0.32
     */
    public String[] allUsersInGroup(String group) throws IOException
    {
        return allUsers("", -1, "", group, "", "");
    }
     
    /**
     *  Returns all usernames of users who are not in given group(s)
     *  @param excludegroup a group name. Use pipe-char "|" to separate if several 
     *  @return (see above)
     *  @throws IOException if a network error occurs
     *  @since 0.32
     */
    public String[] allUsersNotInGroup(String excludegroup) throws IOException
    {
        return allUsers("", -1, "", "", excludegroup, "");
    }
    
    /**
     *  Returns all usernames of users who have given right(s)
     *  @param rights a right name. Use pipe-char "|" to separate if several 
     *  @return (see above)
     *  @throws IOException if a network error occurs
     *  @since 0.32
     */
    public String[] allUsersWithRight(String rights) throws IOException
    {
        return allUsers("", -1, "", "", "", rights);
    }
    
    /**
     *  Returns all usernames with the given prefix.
     *  @param prefix a username prefix (without User:)
     *  @return (see above)
     *  @throws IOException if a network error occurs
     *  @since 0.28
     */
    public String[] allUsersWithPrefix(String prefix) throws IOException
    {
        return allUsers("", -1, prefix, "", "", "");
    }
    
    /**
     *  Gets the specified number of users (as a String) starting at the
     *  given string, in alphabetical order. Equivalent to [[Special:Listusers]].
     *
     *  @param start the string to start enumeration
     *  @param number the number of users to return
     *  @param prefix list all users with this prefix (overrides start and amount),
     *  use "" to not not specify one
     *  @param group list all users in this group(s). Use pipe-char "|" to separate group names.
     *  @param excludegroup list all users who are not in this group(s). Use pipe-char "|" to separate group names.
     *  @param rights list all users with this right(s). Use pipe-char "|" to separate right names.
     *  @return a String[] containing the usernames
     *  @throws IOException if a network error occurs
     *  @since 0.28
     */
    public String[] allUsers(String start, int number, String prefix, String group, String excludegroup, String rights) throws IOException
    {
        // sanitise
        StringBuilder url = new StringBuilder(query);
        url.append("list=allusers&aulimit=");
        String next = "";
        if (prefix.isEmpty())
        {
            url.append( ( number > slowmax || number == -1 ) ? slowmax : number);
            next = encode(start, false);
        }
        else
        {
            url.append(slowmax);
            url.append("&auprefix=");
            url.append(encode(prefix, true));
        }
        if (!group.isEmpty())
        {
            url.append("&augroup=");
            url.append(encode(group, false));
        }
        if (!excludegroup.isEmpty())
        {
            url.append("&auexcludegroup=");
            url.append(encode(excludegroup, false));
        }
        if (!rights.isEmpty())
        {
            url.append("&aurights=");
            url.append(encode(rights, false));
        }
        List<String> members = new ArrayList<>(6667); // enough for most requests
        do
        {
            String temp = url.toString();
            if (!next.isEmpty())
                temp += ("&aufrom=" + encode(next, false));
            String line = fetch(temp, "allUsers");
            
            // bail if nonsense groups/rights
            if (line.contains("Unrecognized values for parameter"))
                return new String[0];
            
            // parse
            next = parseAttribute(line, "aufrom", 0);
            for (int w = line.indexOf("<u "); w > 0; w = line.indexOf("<u ", ++w))
            {
                members.add(parseAttribute(line, "name", w));
                if (members.size() == number)
                {
                    next = null;
                    break;
                }
            }
        }
        while (next != null);
        int size = members.size();
        log(Level.INFO, "allUsers", "Successfully retrieved user list (" + size + " users)");
        return members.toArray(new String[size]);
    }

    /**
     *  Gets the user with the given username. Returns null if it doesn't
     *  exist.
     *  @param username a username
     *  @return the user with that username
     *  @since 0.05
     *  @throws IOException if a network error occurs
     */
    public User getUser(String username) throws IOException
    {
        return userExists(username) ? new User(normalize(username)) : null;
    }

    /**
     *  Gets the user we are currently logged in as. If not logged in, returns
     *  null.
     *  @return the current logged in user
     *  @since 0.05
     */
    public User getCurrentUser()
    {
        return user;
    }

    /**
     *  Gets the contributions of a user in a particular namespace. Equivalent
     *  to [[Special:Contributions]]. Be careful when using this method because
     *  the user may have a high edit count e.g. <tt>enWiki.contribs("MER-C",
     *  Wiki.MAIN_NAMESPACE).length</tt> %gt; 50000.
     *
     *  @param user the user or IP to get contributions for
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the contributions of the user, or a zero length array if the user
     *  does not exist
     *  @throws IOException if a network error occurs
     *  @since 0.17
     */
    public Revision[] contribs(String user, int... ns) throws IOException
    {
        return contribs(user, "", null, null, ns);
    }

    /**
     *  Gets the contributions by a range of IP addresses. Supported ranges are
     *  a whole number of bytes (/8, /16, /24, /32, /40, etc.), anything not a
     *  whole number of bytes is rounded down. WARNING: calls for large IP
     *  ranges may not return for a VERY long time. This may also return edits
     *  by users pretending to be IP addresses e.g. 127.0.0.l.
     *
     *  @param range the CIDR range of IP addresses to get contributions for
     *  @return the contributions of that range
     *  @throws IOException if a network error occurs
     *  @throws UnknownHostException if the IP address is not valid
     *  @throws NumberFormatException if the subnet mask is not valid
     *  @since 0.17
     */
    public Revision[] rangeContribs(String range) throws IOException
    {
        String[] parts = range.split("/");
        String ip = parts[0];
        InetAddress ipaddr = InetAddress.getByName(ip);
        byte[] bytes = ipaddr.getAddress();
        StringBuilder contribuser = new StringBuilder();
        if (ipaddr instanceof Inet6Address)
        {
            // MediaWiki represents IPv6 addresses as 
            // 0:0:0:0:0:0:0:1 or 1234:123:4567:567:AAAA:BBBB:CCCC:DDEF
            // hex letters must be upper case
            int numbytes = (parts.length < 2) ? 16 : (Integer.parseInt(parts[1]) >> 3);
            bytes = Arrays.copyOf(Arrays.copyOf(bytes, numbytes), 16);
            short[] shorts = new short[numbytes >> 1];
            ByteBuffer.wrap(bytes, 0, numbytes).order(ByteOrder.BIG_ENDIAN)
                .asShortBuffer().get(shorts);
            for (int i = 0; i < shorts.length; i++)
            {
                contribuser.append(String.format("%X", shorts[i]));
                if (i != 8)
                    contribuser.append(":");
            } 
            if (numbytes % 2 == 1)
                contribuser.append(String.format("%X", bytes[numbytes - 1]));
        }
        else // IPv4
        {
            int numbytes = (parts.length < 2) ? 4 : (Integer.parseInt(parts[1]) >> 3);
            bytes = Arrays.copyOf(bytes, numbytes);
            for (int i = 0; i < bytes.length; i++)
            {
                contribuser.append(bytes[i]);
                if (i != 3)
                    contribuser.append(".");
            }
        }
        return contribs("", contribuser.toString(), null, null);
    }

    /**
     *  Gets the contributions for a user, an IP address or a range of IP
     *  addresses. Equivalent to [[Special:Contributions]]. To fetch contribs
     *  for an IP range, specify part of an IP address e.g. prefix="127.0."
     *  for 127.0.0.0/16; for IPv6 addresses use e.g. prefix="2001:db8:0:0:0:".
     *  MediaWiki always fully expands IPv6 addresses and converts all digits
     *  A through F to uppercase. (No sanitization is done on IP addresses). Be
     *  careful when using <tt>prefix</tt> as it may take too long and/or cause OOM.
     *
     *  @param user the user to get contributions for.
     *  @param start fetch edits no newer than this date
     *  @param end fetch edits no older than this date
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @param prefix a prefix of usernames. Overrides <tt>user</tt>.  Use "" to
     *  not specify one.
     *  @return contributions of this user, or a zero length array if the user
     *  does not exist
     *  @throws IOException if a network error occurs
     *  @since 0.17
     */
    public Revision[] contribs(String user, String prefix, Calendar end, Calendar start, int... ns) throws IOException
    {
        // prepare the url
        StringBuilder temp = new StringBuilder(query);
        temp.append("list=usercontribs&uclimit=max&ucprop=title%7Ctimestamp%7Cflags%7Ccomment%7Cids%7Csize%7Csizediff&");
        if (prefix.isEmpty())
        {
            temp.append("ucuser=");
            temp.append(encode(user, true));
        }
        else
        {
            temp.append("ucuserprefix=");
            temp.append(prefix);
        }
        constructNamespaceString(temp, "uc", ns);
        // end refers to the *oldest* allowable edit and vice versa
        if (end != null)
        {
            temp.append("&ucend=");
            temp.append(calendarToTimestamp(end));
        }
        List<Revision> revisions = new ArrayList<>(7500);
        String uccontinue = "", ucstart = "";
        if (start != null)
        {
            temp.append("&ucstart=");
            temp.append(calendarToTimestamp(start));
        }

        // fetch data
        do
        {
            String line = fetch(temp.toString() + uccontinue + ucstart, "contribs");

            // set offset parameter
            if (line.contains("uccontinue"))
                uccontinue = "&uccontinue=" + encode(parseAttribute(line, "uccontinue", 0), false);
            else
                uccontinue = null; // depleted list

            // xml form: <item user="Wizardman" ... size="59460" />
            for (int a = line.indexOf("<item "); a > 0; a = line.indexOf("<item ", ++a))
            {
                int b = line.indexOf(" />", a);
                revisions.add(parseRevision(line.substring(a, b), ""));
            }
        }
        while (uccontinue != null);

        // clean up
        int size = revisions.size();
        log(Level.INFO, "contribs", "Successfully retrived contributions for " + (prefix.isEmpty() ? user : prefix) + " (" + size + " edits)");
        return revisions.toArray(new Revision[size]);
    }

    /**
     *  Sends an email message to a user in a similar manner to [[Special:Emailuser]].
     *  You and the target user must have a confirmed email address and the
     *  target user must have email contact enabled. Messages are sent in plain
     *  text (no wiki markup or HTML).
     *
     *  @param user a Wikipedia user with email enabled
     *  @param subject the subject of the message
     *  @param message the plain text message
     *  @param emailme whether to send a copy of the message to your email address
     *  @throws IOException if a network error occurs
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws AccountLockedException if you have been blocked from sending email
     *  @throws UnsupportedOperationException if email is disabled or if you do
     *  not have a verified email address
     *  @since 0.24
     */
    public synchronized void emailUser(User user, String message, String subject, boolean emailme) throws IOException, LoginException
    {
        throttle();

        // check if blocked, logged in
        if (this.user == null || !this.user.isAllowedTo("sendemail"))
            throw new CredentialNotFoundException("Permission denied: cannot email.");

        // is this user emailable?
        if (!(Boolean)user.getUserInfo().get("emailable"))
        {
            // should throw an exception here
            log(Level.WARNING, "emailUser", "User " + user.getUsername() + " is not emailable");
            return;
        }

        // post email
        StringBuilder buffer = new StringBuilder(20000);
        buffer.append("token=");
        buffer.append(encode(getToken("csrf"), false));
        buffer.append("&target=");
        buffer.append(encode(user.getUsername(), false));
        if (emailme)
            buffer.append("&ccme=true");
        buffer.append("&text=");
        buffer.append(encode(message, false));
        buffer.append("&subject=");
        buffer.append(encode(subject, false));
        String response = post(apiUrl + "action=emailuser", buffer.toString(), "emailUser");

        // check for errors
        checkErrorsAndUpdateStatus(response, "email");
        if (response.contains("error code=\"cantsend\""))
            throw new UnsupportedOperationException("Email is disabled for this wiki or you do not have a confirmed email address.");
        log(Level.INFO, "emailUser", "Successfully emailed " + user.getUsername() + ".");
    }

    /**
     *  Unblocks a user. This method is throttled.
     *  @param blockeduser the user to unblock
     *  @param reason the reason for unblocking
     *  @throws CredentialNotFoundException if not an admin
     *  @throws IOException if a network error occurs
     *  @throws CredentialExpiredException if cookies have expired
     *  @throws AccountLockedException if you have been blocked
     *  @since 0.31
     */
    public synchronized void unblock(String blockeduser, String reason) throws IOException, LoginException
    {
        throttle();
        if (user == null || !user.isA("sysop"))
            throw new CredentialNotFoundException("Cannot unblock: permission denied!");

        // send request
        String request = "user=" + encode(blockeduser, false) +
            "&reason=" + encode(reason, false) + "&token=" +
            encode(getToken("csrf"), false);
        String response = post(query + "action=unblock", request, "unblock");

        // done
        try
        {
            if (!response.contains("<unblock "))
                checkErrorsAndUpdateStatus(response, "unblock");
            else if (response.contains("code=\"cantunblock\""))
                log(Level.INFO, "unblock", blockeduser + " is not blocked.");
            else if (response.contains("code=\"blockedasrange\""))
            {
                log(Level.SEVERE, "unblock", "IP " + blockeduser + " is rangeblocked.");
                return; // throw exception?
            }

        }
        catch (IOException e)
        {
            // retry once
            if (retry)
            {
                retry = false;
                log(Level.WARNING, "undelete", "Exception: " + e.getMessage() + " Retrying...");
                unblock(blockeduser, reason);
            }
            else
            {
                log(Level.SEVERE, "unblock", "EXCEPTION: " + e);
                throw e;
            }
        }
        if (retry)
            log(Level.INFO, "unblock", "Successfully unblocked " + blockeduser);
        retry = true;
    }

    // WATCHLIST METHODS

    /**
     *  Adds a page to the watchlist. You need to be logged in to use this.
     *  @param titles the pages to add to the watchlist
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @see #unwatch
     *  @since 0.18
     */
    public void watch(String... titles) throws IOException, CredentialNotFoundException
    {
        watchInternal(false, titles);
        watchlist.addAll(Arrays.asList(titles));
    }

    /**
     *  Removes pages from the watchlist. You need to be logged in to use
     *  this. (Does not do anything if the page is not watched).
     *
     *  @param titles the pages to remove from the watchlist.
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @see #watch
     *  @since 0.18
     */
    public void unwatch(String... titles) throws IOException, CredentialNotFoundException
    {
        watchInternal(true, titles);
        watchlist.removeAll(Arrays.asList(titles));
    }

    /**
     *  Internal method for interfacing with the watchlist, since the API URLs
     *  for (un)watching are very similar.
     *
     *  @param titles the titles to (un)watch
     *  @param unwatch whether we should unwatch these pages
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @see #watch
     *  @see #unwatch
     *  @since 0.18
     */
    protected void watchInternal(boolean unwatch, String... titles) throws IOException, CredentialNotFoundException
    {
        // create the watchlist cache
        String state = unwatch ? "unwatch" : "watch";
        if (watchlist == null)
            getRawWatchlist();
        for (String titlestring : constructTitleString(titles))
        {
            StringBuilder request = new StringBuilder("titles=");
            request.append(titlestring);
            if (unwatch)
                request.append("&unwatch=1");
            request.append("&token=");
            request.append(getToken("watch"));

            post(apiUrl + "action=watch", request.toString(), state);
        }
        log(Level.INFO, state, "Successfully " + state + "ed " + Arrays.toString(titles));
    }

    /**
     *  Fetches the list of titles on the currently logged in user's watchlist.
     *  Equivalent to [[Special:Watchlist/raw]].
     *  @return the contents of the watchlist
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @since 0.18
     */
    public String[] getRawWatchlist() throws IOException, CredentialNotFoundException
    {
        return getRawWatchlist(true);
    }

    /**
     *  Fetches the list of titles on the currently logged in user's watchlist.
     *  Equivalent to [[Special:Watchlist/raw]].
     *  @param cache whether we should use the watchlist cache
     *  (no online activity, if the cache exists)
     *  @return the contents of the watchlist
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @since 0.18
     */
    public String[] getRawWatchlist(boolean cache) throws IOException, CredentialNotFoundException
    {
        // filter anons
        if (user == null)
            throw new CredentialNotFoundException("The watchlist is available for registered users only.");

        // cache
        if (watchlist != null && cache)
            return watchlist.toArray(new String[watchlist.size()]);

        // set up some things
        String url = query + "list=watchlistraw&wrlimit=max";
        String wrcontinue = null;
        watchlist = new ArrayList<>(750);
        // fetch the watchlist
        do
        {
            String line;
            if (wrcontinue == null)
                line = fetch(url, "getRawWatchlist");
            else
                line = fetch(url + "&wrcontinue=" + encode(wrcontinue, false), "getRawWatchlist");
            wrcontinue = parseAttribute(line, "wrcontinue", 0);
            // xml form: <wr ns="14" title="Categorie:Even more things"/>
            for (int a = line.indexOf("<wr "); a > 0; a = line.indexOf("<wr ", ++a))
            {
                String title = parseAttribute(line, "title", a);
                // is this supposed to not retrieve talk pages?
                if (namespace(title) % 2 == 0)
                    watchlist.add(title);
            }
        }
        while (wrcontinue != null);
        // log
        int size = watchlist.size();
        log(Level.INFO, "getRawWatchlist", "Successfully retrieved raw watchlist (" + size + " items)");
        return watchlist.toArray(new String[size]);
    }

    /**
     *  Determines whether a page is watched. (Uses a cache).
     *  @param title the title to be checked
     *  @return whether that page is watched
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @since 0.18
     */
    public boolean isWatched(String title) throws IOException, CredentialNotFoundException
    {
        // populate the watchlist cache
        if (watchlist == null)
            getRawWatchlist();
        return watchlist.contains(title);
    }

    /**
     *  Fetches the most recent changes to pages on your watchlist. Data is
     *  retrieved from the <tt>recentchanges</tt> table and hence cannot be
     *  older than about a month.
     *
     *  @return list of changes to watched pages and their talk pages
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @since 0.27
     */
    public Revision[] watchlist() throws IOException, CredentialNotFoundException
    {
        return watchlist(false);
    }

    /**
     *  Fetches recent changes to pages on your watchlist. Data is retrieved
     *  from the <tt>recentchanges</tt> table and hence cannot be older than
     *  about a month.
     *
     *  @param allrev show all revisions to the pages, instead of the top most
     *  change
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return list of changes to watched pages and their talk pages
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if not logged in
     *  @since 0.27
     */
    public Revision[] watchlist(boolean allrev, int... ns) throws IOException, CredentialNotFoundException
    {
        if (user == null)
            throw new CredentialNotFoundException("Not logged in");
        StringBuilder url = new StringBuilder(query);
        url.append("list=watchlist&wlprop=ids%7Ctitle%7Ctimestamp%7Cuser%7Ccomment%7Csizes&wllimit=max");
        if (allrev)
            url.append("&wlallrev=true");
        constructNamespaceString(url, "wl", ns);

        List<Revision> wl = new ArrayList<>(667);
        String wlstart = "";
        do
        {
            String line = fetch(url.toString() + "&wlstart=" + wlstart, "watchlist");
            wlstart = parseAttribute(line, "wlstart", 0);

            // xml form: <item pageid="16396" revid="176417" ns="0" title="API:Query - Lists" />
            for (int i = line.indexOf("<item "); i > 0; i = line.indexOf("<item ", ++i))
            {
                int j = line.indexOf("/>", i);
                wl.add(parseRevision(line.substring(i, j), ""));
            }
        }
        while (wlstart != null);
        int size = wl.size();
        log(Level.INFO, "watchlist", "Successfully retrieved watchlist (" + size + " items)");
        return wl.toArray(new Revision[size]);
    }

    // LISTS

    /**
     *  Performs a full text search of the wiki. Equivalent to
     *  [[Special:Search]], or that little textbox in the sidebar. Returns an
     *  array of search results, where:
     *  <pre>
     *  results[0] == page name
     *  results[1] == parsed section name (may be "")
     *  results[2] == snippet of page text
     *  </pre>
     *
     *  @param search a search string
     *  @param namespaces the namespaces to search. If no parameters are passed
     *  then the default is MAIN_NAMESPACE only.
     *  @return the search results
     *  @throws IOException if a network error occurs
     *  @since 0.14
     */
    public String[][] search(String search, int... namespaces) throws IOException
    {
        // this varargs thing is really handy, there's no need to define a
        // separate search(String search) while allowing multiple namespaces

        // default to main namespace
        if (namespaces.length == 0)
            namespaces = new int[] { MAIN_NAMESPACE };
        StringBuilder url = new StringBuilder(query);
        url.append("list=search&srwhat=text&srprop=snippet%7Csectionsnippet&srlimit=max&srsearch=");
        url.append(encode(search, false));
        constructNamespaceString(url, "sr", namespaces);
        url.append("&sroffset=");

        // some random variables we need later
        boolean done = false;
        List<String[]> results = new ArrayList<>(5000);

        // fetch and iterate through the search results
        while (!done)
        {
            String line = fetch(url.toString() + results.size(), "search");

            // if this is the last page of results then there is no sroffset parameter
            if (!line.contains("sroffset=\""))
                done = true;

            // xml form: <p ns="0" title="Main Page" snippet="Blah blah blah" sectiontitle="Section"/>
            for (int x = line.indexOf("<p "); x > 0; x = line.indexOf("<p ", ++x))
            {
                String[] result = new String[3];
                result[0] = parseAttribute(line, "title", x);

                // section title (if available). Stupid API documentation is misleading.
                if (line.contains("sectionsnippet=\""))
                    result[1] = parseAttribute(line, "sectionsnippet", x);
                else
                    result[1] = "";

                result[2] = parseAttribute(line, "snippet", x);
                results.add(result);
            }
        }
        log(Level.INFO, "search", "Successfully searched for string \"" + search + "\" (" + results.size() + " items found)");
        return results.toArray(new String[0][0]);
    }

    /**
     *  Returns a list of pages in the specified namespaces which use the
     *  specified image.
     *  @param image the image (may contain File:)
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the list of pages that use this image
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] imageUsage(String image, int... ns) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        image = image.replaceFirst("^(File|Image|" + namespaceIdentifier(FILE_NAMESPACE) + "):", "");
        url.append("list=imageusage&iulimit=max&iutitle=");
        url.append(encode("File:" + image, true));
        constructNamespaceString(url, "iu", ns);

        // fiddle
        List<String> pages = new ArrayList<>(1333);
        String next = "";
        do
        {
            // connect
            if (!pages.isEmpty())
                next = "&iucontinue=" + next;
            String line = fetch(url + next, "imageUsage");
            next = parseAttribute(line, "iucontinue", 0);

            // xml form: <iu pageid="196465" ns="7" title="File talk:Wiki.png" />
            for (int x = line.indexOf("<iu "); x > 0; x = line.indexOf("<iu ", ++x))
                pages.add(parseAttribute(line, "title", x));
        }
        while (next != null);
        int size = pages.size();
        log(Level.INFO, "imageUsage", "Successfully retrieved usages of File:" + image + " (" + size + " items)");
        return pages.toArray(new String[size]);
    }

    /**
     *  Returns a list of all pages linking to this page. Equivalent to
     *  [[Special:Whatlinkshere]].
     *
     *  @param title the title of the page
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the list of pages linking to the specified page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] whatLinksHere(String title, int... ns) throws IOException
    {
        return whatLinksHere(title, false, ns);
    }

    /**
     *  Returns a list of all pages linking to this page within the specified
     *  namespaces. Alternatively, we can retrive a list of what redirects to a
     *  page by setting <tt>redirects</tt> to true. Equivalent to
     *  [[Special:Whatlinkshere]].
     *
     *  @param title the title of the page
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @param redirects whether we should limit to redirects only
     *  @return the list of pages linking to the specified page
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    public String[] whatLinksHere(String title, boolean redirects, int... ns) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("list=backlinks&bllimit=max&bltitle=");
        url.append(encode(title, true));
        constructNamespaceString(url, "bl", ns);
        if (redirects)
            url.append("&blfilterredir=redirects");

        // main loop
        List<String> pages = new ArrayList<>(6667); // generally enough
        String blcontinue = null;
        do
        {
            // fetch data
            String line;
            if (blcontinue == null)
                line = fetch(url.toString(), "whatLinksHere");
            else
                line = fetch(url.toString() + "&blcontinue=" + blcontinue, "whatLinksHere");
            blcontinue = parseAttribute(line, "blcontinue", 0);

            // xml form: <bl pageid="217224" ns="0" title="Mainpage" redirect="" />
            for (int x = line.indexOf("<bl "); x > 0; x = line.indexOf("<bl ", ++x))
                pages.add(parseAttribute(line, "title", x));
        }
        while (blcontinue != null);

        int size = pages.size();
        log(Level.INFO, "whatLinksHere", "Successfully retrieved " + (redirects ? "redirects to " : "links to ") + title + " (" + size + " items)");
        return pages.toArray(new String[size]);
    }

    /**
     *  Returns a list of all pages transcluding to a page within the specified
     *  namespaces.
     *
     *  @param title the title of the page, e.g. "Template:Stub"
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the list of pages transcluding the specified page
     *  @throws IOException if a netwrok error occurs
     *  @since 0.12
     */
    public String[] whatTranscludesHere(String title, int... ns) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("list=embeddedin&eilimit=max&eititle=");
        url.append(encode(title, true));
        constructNamespaceString(url, "ei", ns);

        // main loop
        List<String> pages = new ArrayList<>(6667); // generally enough
        String eicontinue = null;
        do
        {
            // fetch data
            String line;
            if (eicontinue == null)
                line = fetch(url.toString(), "whatTranscludesHere");
            else
                line = fetch(url.toString() + "&eicontinue=" + eicontinue, "whatTranscludesHere");
            eicontinue = parseAttribute(line, "eicontinue", 0);

            // xml form: <ei pageid="7997510" ns="0" title="Maike Evers" />
            for (int x = line.indexOf("<ei "); x > 0; x = line.indexOf("<ei ", ++x))
                pages.add(parseAttribute(line, "title", x));
        }
        while (eicontinue != null);
        int size = pages.size();
        log(Level.INFO, "whatTranscludesHere", "Successfully retrieved transclusions of " + title + " (" + size + " items)");
        return pages.toArray(new String[size]);
    }

    /**
     *  Gets the members of a category.
     *
     *  @param name the name of the category (with or without namespace attached)
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return a String[] containing page titles of members of the category
     *  @throws IOException if a network error occurs
     *  @since 0.03
     */
    public String[] getCategoryMembers(String name, int... ns) throws IOException
    {
        return getCategoryMembers(name, 0, new ArrayList<String>(), ns);
    }

    /**
     *  Gets the members of a category.
     *
     *  @param name the name of the category
     *  @param subcat do you want to return members of sub-categories also? (default: false)
     *  Recursion is limited to a depth of one.
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return a String[] containing page titles of members of the category
     *  @throws IOException if a network error occurs
     *  @since 0.03
     */
    public String[] getCategoryMembers(String name, boolean subcat, int... ns) throws IOException
    {
        return getCategoryMembers(name, (subcat ? 1 : 0), new ArrayList<String>(), ns);
    }

    /**
     *  Gets the members of a category with maxdepth recursion.
     *
     *  Call as <tt>getCategoryMembers("Cat", 999, new int[0])</tt> to avoid
     *  ambiguity.
     *
     *  @param name the name of the category
     *  @param maxdepth depth of recursion for subcategories
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return a String[] containing page titles of members of the category
     *  @throws IOException if a network error occurs
     *  @since 0.31
     */
    public String[] getCategoryMembers(String name, int maxdepth, int... ns) throws IOException
    {
        return getCategoryMembers(name, maxdepth, new ArrayList<String>(), ns);
    }

    /**
     *  Gets the members of a category.
     *
     *  @param name the name of the category
     *  @param maxdepth depth of recursion for subcategories
     *  @param visitedcategories list of already visited categories
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return a String[] containing page titles of members of the category
     *  @throws IOException if a network error occurs
     *  @since 0.03
     */
    protected String[] getCategoryMembers(String name, int maxdepth, List<String> visitedcategories, int... ns) throws IOException
    {
        name = name.replaceFirst("^(Category|" + namespaceIdentifier(CATEGORY_NAMESPACE) + "):", "");
        StringBuilder url = new StringBuilder(query);
        url.append("list=categorymembers&cmprop=title&cmlimit=max&cmtitle=");
        url.append(encode("Category:" + name, true));
        boolean nocat = ns.length != 0;
        if (maxdepth > 0 && nocat)
        {
            for (int i = 0; nocat && i < ns.length; i++)
                nocat = (ns[i] != CATEGORY_NAMESPACE);
            if (nocat)
            {
                int[] temp = Arrays.copyOf(ns, ns.length + 1);
                temp[ns.length] = CATEGORY_NAMESPACE;
                constructNamespaceString(url, "cm", temp);
            }
            else
                constructNamespaceString(url, "cm", ns);
        }
        else
            constructNamespaceString(url, "cm", ns);
        List<String> members = new ArrayList<>();
        String next = "";
        do
        {
            if (!next.isEmpty())
                next = "&cmcontinue=" + encode(next, false);
            String line = fetch(url.toString() + next, "getCategoryMembers");
            next = parseAttribute(line, "cmcontinue", 0);

            // xml form: <cm pageid="24958584" ns="3" title="User talk:86.29.138.185" />
            for (int x = line.indexOf("<cm "); x > 0; x = line.indexOf("<cm ", ++x))
            {
                String member = parseAttribute(line, "title", x);

                // fetch subcategories
                boolean iscat = namespace(member) == CATEGORY_NAMESPACE;
                if (maxdepth > 0 && iscat && !visitedcategories.contains(member))
                {
                    visitedcategories.add(member);
                    String[] categoryMembers = getCategoryMembers(member, maxdepth - 1, visitedcategories, ns);
                    members.addAll(Arrays.asList(categoryMembers));
                }

                // ignore this item if we requested subcat but not CATEGORY_NAMESPACE
                if (!(maxdepth > 0) || !nocat || !iscat)
                    members.add(member);
            }
        }
        while (next != null);

        int size = members.size();
        log(Level.INFO, "getCategoryMembers", "Successfully retrieved contents of Category:" + name + " (" + size + " items)");
        return members.toArray(new String[size]);
    }

    /**
     *  Searches the wiki for external links. Equivalent to [[Special:Linksearch]].
     *  Returns two lists, where the first is the list of pages and the
     *  second is the list of urls. The index of a page in the first list
     *  corresponds to the index of the url on that page in the second list.
     *  Wildcards (*) are only permitted at the start of the search string.
     *
     *  @param pattern the pattern (String) to search for (e.g. example.com,
     *  *.example.com)
     *  @throws IOException if a network error occurs
     *  @return two lists - index 0 is the list of pages (String), index 1 is
     *  the list of urls (instance of <tt>java.net.URL</tt>)
     *  @since 0.06
     */
    public List[] linksearch(String pattern) throws IOException
    {
        return linksearch(pattern, "http");
    }

    /**
     *  Searches the wiki for external links. Equivalent to [[Special:Linksearch]].
     *  Returns two lists, where the first is the list of pages and the
     *  second is the list of urls. The index of a page in the first list
     *  corresponds to the index of the url on that page in the second list.
     *  Wildcards (*) are only permitted at the start of the search string.
     *
     *  @param pattern the pattern (String) to search for (e.g. example.com,
     *  *.example.com)
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @param protocol one of { http, https, ftp, irc, gopher, telnet, nntp,
     *  worldwind, mailto, news, svn, git, mms } or "" (equivalent to http)
     *  @throws IOException if a network error occurs
     *  @return two lists - index 0 is the list of pages (String), index 1 is
     *  the list of urls (instance of <tt>java.net.URL</tt>)
     *  @since 0.24
     */
    public List[] linksearch(String pattern, String protocol, int... ns) throws IOException
    {
        // FIXME: Change return type to ArrayList<Object[]> or Object[][]
        // First index refers to item number, linksearch()[x][0] = page title

        // set it up
        StringBuilder url = new StringBuilder(query);
        url.append("list=exturlusage&euprop=title%7curl&eulimit=max&euquery=");
        url.append(pattern);
        url.append("&euprotocol=");
        url.append(protocol);
        constructNamespaceString(url, "eu", ns);
        url.append("&euoffset=");

        // some variables we need later
        List[] ret = new ArrayList[] // no reason for more than 500 links
        {
            new ArrayList<String>(667), // page titles
            new ArrayList<URL>(667) // urls
        };
        String euoffset = "0";
        // begin
        do
        {
            // if this is the last page of results then there is no euoffset parameter
            String line = fetch(url.toString() + euoffset, "linksearch");
            euoffset = parseAttribute(line, "euoffset", 0);

            // xml form: <eu ns="0" title="Main Page" url="http://example.com" />
            for (int x = line.indexOf("<eu"); x > 0; x = line.indexOf("<eu ", ++x))
            {
                String link = parseAttribute(line, "url", x);
                ret[0].add(parseAttribute(line, "title", x));
                if (link.charAt(0) == '/') // protocol relative url
                    ret[1].add(new URL(protocol + ":" + link));
                else
                    ret[1].add(new URL(link));
            }
        }
        while (euoffset != null);

        // return value
        log(Level.INFO, "linksearch", "Successfully returned instances of external link " + pattern + " (" + ret[0].size() + " links)");
        return ret;
    }

    /**
     *  Looks up a particular user in the IP block list, i.e. whether a user
     *  is currently blocked. Equivalent to [[Special:BlockList]].
     *
     *  @param user a username or IP (e.g. "127.0.0.1")
     *  @return the block log entry
     *  @throws IOException if a network error occurs
     *  @since 0.12
     */
    public LogEntry[] getIPBlockList(String user) throws IOException
    {
        return getIPBlockList(user, null, null);
    }

    /**
     *  Lists currently operating blocks that were made in the specified
     *  interval. Equivalent to [[Special:BlockList]].
     *
     *  @param start the start date
     *  @param end the end date
     *  @return the currently operating blocks that were made in that interval
     *  @throws IOException if a network error occurs
     *  @since 0.12
     */
    public LogEntry[] getIPBlockList(Calendar start, Calendar end) throws IOException
    {
        return getIPBlockList("", start, end);
    }

    /**
     *  Fetches part of the list of currently operational blocks. Equivalent to
     *  [[Special:BlockList]]. WARNING: cannot tell whether a particular IP
     *  is autoblocked as this is non-public data (see [[wmf:Privacy policy]]).
     *  Don't call this directly, use one of the two above methods instead.
     *
     *  @param user a particular user that might have been blocked. Use "" to
     *  not specify one. May be an IP (e.g. "127.0.0.1") or a CIDR range (e.g.
     *  "127.0.0.0/16") but not an autoblock (e.g. "#123456").
     *  @param start what timestamp to start. Use null to not specify one.
     *  @param end what timestamp to end. Use null to not specify one.
     *  @return a LogEntry[] of the blocks
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if start date is before end date
     *  @since 0.12
     */
    protected LogEntry[] getIPBlockList(String user, Calendar start, Calendar end) throws IOException
    {
        // quick param check
        if (start != null && end != null)
            if (start.before(end))
                throw new IllegalArgumentException("Specified start date is before specified end date!");
        String bkstart = calendarToTimestamp(start == null ? makeCalendar() : start);

        // url base
        StringBuilder urlBase = new StringBuilder(query);
        urlBase.append("list=blocks&bklimit=");
        urlBase.append(max);
        if (end != null)
        {
            urlBase.append("&bkend=");
            urlBase.append(calendarToTimestamp(end));
        }
        if (!user.isEmpty())
        {
            urlBase.append("&bkusers=");
            urlBase.append(user);
        }
        urlBase.append("&bkstart=");

        // connection
        List<LogEntry> entries = new ArrayList<>(1333);
        do
        {
            String line = fetch(urlBase.toString() + bkstart, "getIPBlockList");
            bkstart = parseAttribute(line, "bkstart", 0);

            // parse xml
            for (int a = line.indexOf("<block "); a > 0; a = line.indexOf("<block ", ++a))
            {
                // find entry
                int b = line.indexOf("/>", a);
                String temp = line.substring(a, b);
                LogEntry le = parseLogEntry(temp);
                le.type = BLOCK_LOG;
                le.action = "block";
                // parseLogEntries parses block target into le.user due to mw.api
                // attribute name
                if (le.user == null) // autoblock
                    le.target = "#" + parseAttribute(temp, "id", 0);
                else
                    le.target = namespaceIdentifier(USER_NAMESPACE) + ":" + le.user.username;
                // parse blocker for real
                le.user = new User(parseAttribute(temp, "by", 0));
                entries.add(le);
            }
        }
        while (bkstart != null);

        // log statement
        StringBuilder logRecord = new StringBuilder("Successfully fetched IP block list ");
        if (!user.isEmpty())
        {
            logRecord.append(" for ");
            logRecord.append(user);
        }
        if (start != null)
        {
            logRecord.append(" from ");
            logRecord.append(start.getTime().toString());
        }
        if (end != null)
        {
            logRecord.append(" to ");
            logRecord.append(end.getTime().toString());
        }
        int size = entries.size();
        logRecord.append(" (");
        logRecord.append(size);
        logRecord.append(" entries)");
        log(Level.INFO, "getIPBlockList", logRecord.toString());
        return entries.toArray(new LogEntry[size]);
    }

    /**
     *  Gets the most recent set of log entries up to the given amount.
     *  Equivalent to [[Special:Log]].
     *
     *  @param amount the amount of log entries to get
     *  @return the most recent set of log entries
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if amount &lt; 1
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(int amount) throws IOException
    {
        return getLogEntries(null, null, amount, ALL_LOGS, "", null, "", ALL_NAMESPACES);
    }

    /**
     *  Gets log entries for a specific user. Equivalent to [[Special:Log]].
     *  @param user the user to get log entries for
     *  @throws IOException if a network error occurs
     *  @return the set of log entries created by that user
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(User user) throws IOException
    {
        return getLogEntries(null, null, Integer.MAX_VALUE, ALL_LOGS, "", user, "", ALL_NAMESPACES);
    }

    /**
     *  Gets the log entries representing actions that were performed on a
     *  specific target. Equivalent to [[Special:Log]].
     *
     *  @param target the target of the action(s).
     *  @throws IOException if a network error occurs
     *  @return the specified log entries
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(String target) throws IOException
    {
        return getLogEntries(null, null, Integer.MAX_VALUE, ALL_LOGS, "", null, target, ALL_NAMESPACES);
    }

    /**
     *  Gets all log entries that occurred between the specified dates.
     *  WARNING: the start date is the most recent of the dates given, and
     *  the order of enumeration is from newest to oldest. Equivalent to
     *  [[Special:Log]].
     *
     *  @param start what timestamp to start. Use null to not specify one.
     *  @param end what timestamp to end. Use null to not specify one.
     *  @throws IOException if something goes wrong
     *  @throws IllegalArgumentException if start &lt; end
     *  @return the specified log entries
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(Calendar start, Calendar end) throws IOException
    {
        return getLogEntries(start, end, Integer.MAX_VALUE, ALL_LOGS, "", null, "", ALL_NAMESPACES);
    }

    /**
     *  Gets the last how ever many log entries in the specified log. Equivalent
     *  to [[Special:Log]] and [[Special:Newimages]] when
     *  <tt>type.equals(UPLOAD_LOG)</tt>.
     *
     *  @param amount the number of entries to get
     *  @param type what log to get (e.g. DELETION_LOG)
     *  @param action what action to get (e.g. delete, undelete, etc.), use "" to
     *  not specify one
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if the log type doesn't exist
     *  @return the specified log entries
     */
    public LogEntry[] getLogEntries(int amount, String type, String action) throws IOException
    {
        return getLogEntries(null, null, amount, type, action, null, "", ALL_NAMESPACES);
    }

    /**
     *  Gets the specified amount of log entries between the given times by
     *  the given user on the given target. Equivalent to [[Special:Log]].
     *  WARNING: the start date is the most recent of the dates given, and
     *  the order of enumeration is from newest to oldest.
     *
     *  @param start what timestamp to start. Use null to not specify one.
     *  @param end what timestamp to end. Use null to not specify one.
     *  @param amount the amount of log entries to get. If both start and
     *  end are defined, this is ignored. Use Integer.MAX_VALUE to not
     *  specify one.
     *  @param log what log to get (e.g. DELETION_LOG)
     *  @param action what action to get (e.g. delete, undelete, etc.), use "" to
     *  not specify one
     *  @param user the user performing the action. Use null not to specify
     *  one.
     *  @param target the target of the action. Use "" not to specify one.
     *  @param namespace filters by namespace. Returns empty if namespace
     *  doesn't exist. Use ALL_NAMESPACES to not specify one.
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if start &lt; end or amount &lt; 1
     *  @return the specified log entries
     *  @since 0.08
     */
    public LogEntry[] getLogEntries(Calendar start, Calendar end, int amount, String log, String action,
        User user, String target, int namespace) throws IOException
    {
        // construct the query url from the parameters given
        StringBuilder url = new StringBuilder(query);
        url.append("list=logevents&leprop=title%7Ctype%7Cuser%7Ctimestamp%7Ccomment%7Cdetails&lelimit=");

        // check for amount
        if (amount < 1)
            throw new IllegalArgumentException("Tried to retrieve less than one log entry!");
        url.append(amount > max ? max : amount);

        // log type
        if (!log.equals(ALL_LOGS))
        {
            if (action.isEmpty())
            {
                url.append("&letype=");
                url.append(log);
            }
            else
            {
                url.append("&leaction=");
                url.append(log);
                url.append("/");
                url.append(action);
            }
        }

        if (namespace != ALL_NAMESPACES)
        {
            url.append("&lenamespace=");
            url.append(namespace);
        }
        if (user != null)
        {
            url.append("&leuser=");
            // should already be normalized since we have a User object
            url.append(encode(user.getUsername(), false));
        }
        if (!target.isEmpty())
        {
            url.append("&letitle=");
            url.append(encode(target, true));
        }

        // check for start/end dates
        String lestart = ""; // we need to account for lestart being the continuation parameter too.
        if (start != null)
        {
            if (end != null && start.before(end)) //aargh
                throw new IllegalArgumentException("Specified start date is before specified end date!");
            lestart = calendarToTimestamp(start);
        }
        if (end != null)
        {
            url.append("&leend=");
            url.append(calendarToTimestamp(end));
        }

        // only now we can actually start to retrieve the logs
        List<LogEntry> entries = new ArrayList<>(6667); // should be enough
        do
        {
            String line = fetch(url.toString() + "&lestart=" + lestart, "getLogEntries");
            lestart = parseAttribute(line, "lestart", 0);

            // parse xml. We need to repeat the test because the XML may contain more than the required amount.
            while (line.contains("<item") && entries.size() < amount)
            {
                // find entry
                int a = line.indexOf("<item");
                // end may be " />" or "</item>", followed by next item
                int b = line.indexOf("><item", a);
                if (b < 0) // last entry
                    b = line.length();
                entries.add(parseLogEntry(line.substring(a, b)));
                line = line.substring(b);
            }
        }
        while (entries.size() < amount && lestart != null);

        // log the success
        StringBuilder console = new StringBuilder("Successfully retrieved log (type=");
        console.append(log);
        int size = entries.size();
        console.append(", ");
        console.append(size);
        console.append(" entries)");
        log(Level.INFO, "getLogEntries", console.toString());
        return entries.toArray(new LogEntry[size]);
    }

    /**
     *  Parses xml generated by <tt>getLogEntries()</tt>,
     *  <tt>getImageHistory()</tt> and <tt>getIPBlockList()</tt> into LogEntry
     *  objects. Override this if you want custom log types. NOTE: if
     *  RevisionDelete was used on a log entry, the relevant values will be
     *  null.
     *
     *  @param xml the xml to parse
     *  @return the parsed log entry
     *  @since 0.18
     */
    protected LogEntry parseLogEntry(String xml)
    {
        // note that we can override these in the calling method
        String type = "", action = "";
        if (xml.contains("type=\"")) // only getLogEntries
        {
            type = parseAttribute(xml, "type", 0);
            if (!xml.contains("actionhidden=\"")) // not oversighted
                action = parseAttribute(xml, "action", 0);
        }

        // reason
        String reason;
        if (xml.contains("commenthidden=\""))
            reason = null;
        else if (type.equals(USER_CREATION_LOG)) // there is no reason for creating a user
            reason = "";
        else if (xml.contains("reason=\""))
            reason = parseAttribute(xml, "reason", 0);
        else
            reason = parseAttribute(xml, "comment", 0);

        // generic performer name (won't work for ipblocklist, overridden there)
        User performer = null;
        if (xml.contains("user=\""))
            performer = new User(parseAttribute(xml, "user", 0));

        // generic target name
        String target = null;
        if (xml.contains(" title=\"")) // space is important -- commons.getImageHistory("File:Chief1.gif");
            target = parseAttribute(xml, "title", 0);

        String timestamp = convertTimestamp(parseAttribute(xml, "timestamp", 0));

        // details: TODO: make this a HashMap
        Object details = null;
        if (xml.contains("commenthidden")) // oversighted
            details = null;
        else if (type.equals(MOVE_LOG))
            details = parseAttribute(xml, "target_title", 0); // the new title
        else if (type.equals(BLOCK_LOG) || xml.contains("<block"))
        {
            int a = xml.indexOf("<block") + 7;
            String s = xml.substring(a);
            int c = xml.contains("expiry=\"") ? s.indexOf("expiry=") + 8 : s.indexOf("duration=") + 10;
            if (c > 10) // not an unblock
            {
                int d = s.indexOf('\"', c);
                details = new Object[]
                {
                    s.contains("anononly"), // anon-only
                    s.contains("nocreate"), // account creation blocked
                    s.contains("noautoblock"), // autoblock disabled
                    s.contains("noemail"), // email disabled
                    s.contains("nousertalk"), // cannot edit talk page
                    s.substring(c, d) // duration
                };
            }
        }
        else if (type.equals(PROTECTION_LOG))
        {
            if (action.equals("unprotect"))
                details = null;
            else
            {
                // FIXME: return a protectionstate here?
                int a = xml.indexOf("<param>") + 7;
                int b = xml.indexOf("</param>", a);
                details = xml.substring(a, b);
            }
        }
        else if (type.equals(USER_RENAME_LOG))
        {
            int a = xml.indexOf("<param>") + 7;
            int b = xml.indexOf("</param>", a);
            details = decode(xml.substring(a, b)); // the new username
        }
        else if (type.equals(USER_RIGHTS_LOG))
        {
            int a = xml.indexOf("new=\"") + 5;
            int b = xml.indexOf('\"', a);
            StringTokenizer tk = new StringTokenizer(xml.substring(a, b), ", ");
            List<String> temp = new ArrayList<>();
            while (tk.hasMoreTokens())
                temp.add(tk.nextToken());
            details = temp.toArray(new String[temp.size()]);
        }

        return new LogEntry(type, action, reason, performer, target, timestamp, details);
    }

    /**
     *  Lists pages that start with a given prefix. Equivalent to
     *  [[Special:Prefixindex]].
     *
     *  @param prefix the prefix
     *  @return the list of pages with that prefix
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] prefixIndex(String prefix) throws IOException
    {
        return listPages(prefix, null, ALL_NAMESPACES, -1, -1, null);
    }

    /**
     *  List pages below a certain size in the main namespace. Equivalent to
     *  [[Special:Shortpages]].
     *  @param cutoff the maximum size in bytes these short pages can be
     *  @return pages below that size
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] shortPages(int cutoff) throws IOException
    {
        return listPages("", null, MAIN_NAMESPACE, -1, cutoff, null);
    }

    /**
     *  List pages below a certain size in any namespace. Equivalent to
     *  [[Special:Shortpages]].
     *  @param cutoff the maximum size in bytes these short pages can be
     *  @param namespace a namespace
     *  @throws IOException if a network error occurs
     *  @return pages below that size in that namespace
     *  @since 0.15
     */
    public String[] shortPages(int cutoff, int namespace) throws IOException
    {
        return listPages("", null, namespace, -1, cutoff, null);
    }

    /**
     *  List pages above a certain size in the main namespace. Equivalent to
     *  [[Special:Longpages]].
     *  @param cutoff the minimum size in bytes these long pages can be
     *  @return pages above that size
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] longPages(int cutoff) throws IOException
    {
        return listPages("", null, MAIN_NAMESPACE, cutoff, -1, null);
    }

    /**
     *  List pages above a certain size in any namespace. Equivalent to
     *  [[Special:Longpages]].
     *  @param cutoff the minimum size in nbytes these long pages can be
     *  @param namespace a namespace
     *  @return pages above that size
     *  @throws IOException if a network error occurs
     *  @since 0.15
     */
    public String[] longPages(int cutoff, int namespace) throws IOException
    {
        return listPages("", null, namespace, cutoff, -1, null);
    }

    /**
     *  Lists pages with titles containing a certain prefix with a certain
     *  protection state and in a certain namespace. Equivalent to
     *  [[Special:Allpages]], [[Special:Prefixindex]], [[Special:Protectedpages]]
     *  and [[Special:Allmessages]] (if namespace == MEDIAWIKI_NAMESPACE).
     *  WARNING: Limited to 500 values (5000 for bots), unless a prefix or
     *  protection level is specified.
     *
     *  @param prefix the prefix of the title. Use "" to not specify one.
     *  @param protectionstate a {@link #protect protection state}, use null
     *  to not specify one
     *  @param namespace a namespace. ALL_NAMESPACES is not suppported, an
     *  UnsupportedOperationException will be thrown.
     *  @return the specified list of pages
     *  @since 0.09
     *  @throws IOException if a network error occurs
     */
    public String[] listPages(String prefix, Map<String, Object> protectionstate, int namespace) throws IOException
    {
        return listPages(prefix, protectionstate, namespace, -1, -1, null);
    }

    /**
     *  Lists pages with titles containing a certain prefix with a certain
     *  protection state and in a certain namespace. Equivalent to
     *  [[Special:Allpages]], [[Special:Prefixindex]], [[Special:Protectedpages]]
     *  [[Special:Allmessages]] (if namespace == MEDIAWIKI_NAMESPACE),
     *  [[Special:Shortpages]] and [[Special:Longpages]]. WARNING: Limited to
     *  500 values (5000 for bots), unless a prefix, (max|min)imum size or
     *  protection level is specified.
     *
     *  @param prefix the prefix of the title. Use "" to not specify one.
     *  @param protectionstate a {@link #protect protection state}, use null
     *  to not specify one
     *  @param namespace a namespace. ALL_NAMESPACES is not suppported, an
     *  UnsupportedOperationException will be thrown.
     *  @param minimum the minimum size in bytes these pages can be. Use -1 to
     *  not specify one.
     *  @param maximum the maximum size in bytes these pages can be. Use -1 to
     *  not specify one.
     *  @param redirects Boolean.TRUE = list redirects only, Boolean.FALSE = list
     *  non-redirects only, null = list both
     *  @return the specified list of pages
     *  @since 0.09
     *  @throws IOException if a network error occurs
     */
    public String[] listPages(String prefix, Map<String, Object> protectionstate, int namespace, int minimum,
        int maximum, Boolean redirects) throws IOException
    {
        // @revised 0.15 to add short/long pages
        // No varargs namespace here because MW API only supports one namespace
        // for this module.
        StringBuilder url = new StringBuilder(query);
        url.append("list=allpages&aplimit=max");
        if (!prefix.isEmpty()) // prefix
        {
            // cull the namespace prefix
            namespace = namespace(prefix);
            if (prefix.contains(":") && namespace != MAIN_NAMESPACE)
                prefix = prefix.substring(prefix.indexOf(':') + 1);
            url.append("&apprefix=");
            url.append(encode(prefix, true));
        }
        else if (namespace == ALL_NAMESPACES) // check for namespace
            throw new UnsupportedOperationException("ALL_NAMESPACES not supported in MediaWiki API.");
        url.append("&apnamespace=");
        url.append(namespace);
        if (protectionstate != null)
        {
            StringBuilder apprtype = new StringBuilder("&apprtype=");
            StringBuilder apprlevel = new StringBuilder("&apprlevel=");
            for (Map.Entry<String, Object> entry : protectionstate.entrySet())
            {
                String key = entry.getKey();
                if (key.equals("cascade"))
                {
                    url.append("&apprfiltercascade=");
                    url.append((Boolean)entry.getValue() ? "cascading" : "noncascading");
                }
                else if (!key.contains("expiry"))
                {
                    apprtype.append(key);
                    apprtype.append("%7C");
                    apprlevel.append((String)entry.getValue());
                    apprlevel.append("%7C");
                }
            }
            apprtype.delete(apprtype.length() - 3, apprtype.length());
            apprlevel.delete(apprlevel.length() - 3, apprlevel.length());
            url.append(apprtype);
            url.append(apprlevel);
        }
        // max and min
        if (minimum != -1)
        {
            url.append("&apminsize=");
            url.append(minimum);
        }
        if (maximum != -1)
        {
            url.append("&apmaxsize=");
            url.append(maximum);
        }
        if (redirects == Boolean.TRUE)
            url.append("&apfilterredir=redirects");
        else if (redirects == Boolean.FALSE)
            url.append("&apfilterredir=nonredirects");

        // parse
        List<String> pages = new ArrayList<>(6667);
        String next = null;
        do
        {
            // connect and read
            String s = url.toString();
            if (next != null)
                s += ("&apcontinue=" + encode(next, false));
            String line = fetch(s, "listPages");

            // don't set a continuation if no max, min, prefix or protection level
            if (maximum < 0 && minimum < 0 && prefix.isEmpty() && protectionstate == null)
                next = null;
            // find next value
            else if (line.contains("apcontinue="))
                next = parseAttribute(line, "apcontinue", 0);
            else
                next = null;

            // xml form: <p pageid="1756320" ns="0" title="Kre'fey" />
            for (int a = line.indexOf("<p "); a > 0; a = line.indexOf("<p ", ++a))
                pages.add(parseAttribute(line, "title", a));
        }
        while (next != null);

        // tidy up
        int size = pages.size();
        log(Level.INFO, "listPages", "Successfully retrieved page list (" + size + " pages)");
        return pages.toArray(new String[size]);
    }

    /**
     *  Fetches data from one of a set of miscellaneous special pages.
     *  WARNING: some of these may be *CACHED*, *DISABLED* and/or *LIMITED* on
     *  large wikis.
     *
     *  @param page one of { Ancientpages, BrokenRedirects, Deadendpages,
     *  Disambiguations, DoubleRedirects, Listredirects, Lonelypages, Longpages,
     *  Mostcategories, Mostimages, Mostinterwikis, Mostlinkedcategories,
     *  Mostlinkedtemplates, Mostlinked, Mostrevisions, Fewestrevisions, Shortpages,
     *  Uncategorizedcategories, Uncategorizedpages, Uncategorizedimages,
     *  Uncategorizedtemplates, Unusedcategories, Unusedimages, Wantedcategories,
     *  Wantedfiles, Wantedpages, Wantedtemplates, Unwatchedpages, Unusedtemplates,
     *  Withoutinterwiki }. This parameter is *case sensitive*.
     *  @return the list of pages returned by that particular special page
     *  @throws IOException if a network error occurs
     *  @throws CredentialNotFoundException if page=Unwatchedpages and we cannot
     *  read it
     *  @since 0.28
     */
    public String[] queryPage(String page) throws IOException, CredentialNotFoundException
    {
        if (page.equals("Unwatchedpages") && (user == null || !user.isAllowedTo("unwatchedpages")))
            throw new CredentialNotFoundException("User does not have the \"unwatchedpages\" permission.");

        String url = query + "action=query&list=querypage&qplimit=max&qppage=" + page + "&qpcontinue=";
        String offset = "";
        List<String> pages = new ArrayList<>(1333);

        do
        {
            String line = fetch(url + offset, "queryPage");
            offset = parseAttribute(line, "qpoffset", 0);

            // xml form: <page value="0" ns="0" title="Anorthosis Famagusta FC in European football" />
            for (int x = line.indexOf("<page "); x > 0; x = line.indexOf("<page ", ++x))
                pages.add(parseAttribute(line, "title", x));
        }
        while (offset != null);
        int temp = pages.size();
        log(Level.INFO, "queryPage", "Successfully retrieved [[Special:" + page + "]] (" + temp + " pages)");
        return pages.toArray(new String[temp]);
    }

    /**
     *  Fetches the <tt>amount</tt> most recently created pages in the main
     *  namespace. WARNING: The recent changes table only stores new pages
     *  for about a month. It is not possible to retrieve changes before then.
     *
     *  @param amount the number of pages to fetch
     *  @return the revisions that created the pages satisfying the requirements
     *  above
     *  @throws IOException if a network error occurs
     *  @since 0.20
     */
    public Revision[] newPages(int amount) throws IOException
    {
        return recentChanges(amount, 0, true, MAIN_NAMESPACE);
    }

    /**
     *  Fetches the <tt>amount</tt> most recently created pages in the main
     *  namespace subject to the specified constraints. WARNING: The
     *  recent changes table only stores new pages for about a month. It is not
     *  possible to retrieve changes before then. Equivalent to
     *  [[Special:Newpages]].
     *
     *  @param rcoptions a bitmask of HIDE_ANON etc that dictate which pages
     *  we return (e.g. to exclude patrolled pages set rcoptions = HIDE_PATROLLED).
     *  @param amount the amount of new pages to get
     *  @return the revisions that created the pages satisfying the requirements
     *  above
     *  @throws IOException if a network error occurs
     *  @since 0.20
     */
    public Revision[] newPages(int amount, int rcoptions) throws IOException
    {
        return recentChanges(amount, rcoptions, true, MAIN_NAMESPACE);
    }

    /**
     *  Fetches the <tt>amount</tt> most recently created pages in the
     *  specified namespace, subject to the specified constraints. WARNING: The
     *  recent changes table only stores new pages for about a month. It is not
     *  possible to retrieve changes before then. Equivalent to
     *  [[Special:Newpages]].
     *
     *  @param rcoptions a bitmask of HIDE_ANON etc that dictate which pages
     *  we return (e.g. to exclude patrolled pages set rcoptions = HIDE_PATROLLED).
     *  @param amount the amount of new pages to get
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the revisions that created the pages satisfying the requirements
     *  above
     *  @throws IOException if a network error occurs
     *  @since 0.20
     */
    public Revision[] newPages(int amount, int rcoptions, int... ns) throws IOException
    {
        // @revised 0.23 move code to recent changes
        return recentChanges(amount, rcoptions, true, ns);
    }

    /**
     *  Fetches the <tt>amount</tt> most recent changes in the main namespace.
     *  WARNING: The recent changes table only stores new pages for about a
     *  month. It is not possible to retrieve changes before then. Equivalent
     *  to [[Special:Recentchanges]].
     *  <p>
     *  Note: Log entries in recent changes have a revid of 0!
     *
     *  @param amount the number of entries to return
     *  @return the recent changes that satisfy these criteria
     *  @throws IOException if a network error occurs
     *  @since 0.23
     */
    public Revision[] recentChanges(int amount) throws IOException
    {
        return recentChanges(amount, 0, false, MAIN_NAMESPACE);
    }

    /**
     *  Fetches the <tt>amount</tt> most recent changes in the specified
     *  namespace. WARNING: The recent changes table only stores new pages for
     *  about a month. It is not possible to retrieve changes before then.
     *  Equivalent to [[Special:Recentchanges]].
     *  <p>
     *  Note: Log entries in recent changes have a revid of 0!
     *
     *  @param amount the number of entries to return
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @return the recent changes that satisfy these criteria
     *  @throws IOException if a network error occurs
     *  @since 0.23
     */
    public Revision[] recentChanges(int amount, int... ns) throws IOException
    {
        return recentChanges(amount, 0, false, ns);
    }

    /**
     *  Fetches the <tt>amount</tt> most recent changes in the specified
     *  namespace subject to the specified constraints. WARNING: The recent
     *  changes table only stores new pages for about a month. It is not
     *  possible to retrieve changes before then. Equivalent to
     *  [[Special:Recentchanges]].
     *  <p>
     *  Note: Log entries in recent changes have a revid of 0!
     *
     *  @param amount the number of entries to return
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @param rcoptions a bitmask of HIDE_ANON etc that dictate which pages
     *  we return.
     *  @return the recent changes that satisfy these criteria
     *  @throws IOException if a network error occurs
     *  @since 0.23
     */
    public Revision[] recentChanges(int amount, int rcoptions, int... ns) throws IOException
    {
        return recentChanges(amount, rcoptions, false, ns);
    }

    /**
     *  Fetches the <tt>amount</tt> most recent changes in the specified
     *  namespace subject to the specified constraints. WARNING: The recent
     *  changes table only stores new pages for about a month. It is not
     *  possible to retrieve changes before then. Equivalent to
     *  [[Special:Recentchanges]].
     *  <p>
     *  Note: Log entries in recent changes have a revid of 0!
     *
     *  @param amount the number of entries to return
     *  @param ns a list of namespaces to filter by, empty = all namespaces.
     *  @param rcoptions a bitmask of HIDE_ANON etc that dictate which pages
     *  we return.
     *  @param newpages show new pages only
     *  @return the recent changes that satisfy these criteria
     *  @throws IOException if a network error occurs
     *  @since 0.23
     */
    protected Revision[] recentChanges(int amount, int rcoptions, boolean newpages, int... ns) throws IOException
    {
        StringBuilder url = new StringBuilder(query);
        url.append("list=recentchanges&rcprop=title%7Cids%7Cuser%7Ctimestamp%7Cflags%7Ccomment%7Csizes&rclimit=max");
        constructNamespaceString(url, "rc", ns);
        if (newpages)
            url.append("&rctype=new");
        // rc options
        if (rcoptions > 0)
        {
            url.append("&rcshow=");
            if ((rcoptions & HIDE_ANON) == HIDE_ANON)
                url.append("!anon%7C");
            if ((rcoptions & HIDE_SELF) == HIDE_SELF)
                url.append("!self%7C");
            if ((rcoptions & HIDE_MINOR) == HIDE_MINOR)
                url.append("!minor%7C");
            if ((rcoptions & HIDE_PATROLLED) == HIDE_PATROLLED)
                url.append("!patrolled%7C");
            if ((rcoptions & HIDE_BOT) == HIDE_BOT)
                url.append("!bot%7C");
            // chop off last |
            url.delete(url.length() - 3, url.length());
        }

        // fetch, parse
        url.append("&rcstart=");
        String rcstart = calendarToTimestamp(makeCalendar());
        List<Revision> revisions = new ArrayList<>(750);
        do
        {
            String temp = url.toString();
            String line = fetch(temp + rcstart, newpages ? "newPages" : "recentChanges");

            // set continuation parameter
            rcstart = parseAttribute(line, "rcstart", 0);

            // xml form <rc type="edit" ns="0" title="Main Page" ... />
            for (int i = line.indexOf("<rc "); i > 0 && revisions.size() < amount; i = line.indexOf("<rc ", ++i))
            {
                int j = line.indexOf("/>", i);
                revisions.add(parseRevision(line.substring(i, j), ""));
            }
        }
        while (revisions.size() < amount);
        int temp = revisions.size();
        log(Level.INFO, "recentChanges", "Successfully retrieved recent changes (" + temp + " revisions)");
        return revisions.toArray(new Revision[temp]);
    }

    /**
     *  Fetches all pages that use interwiki links to the specified wiki and the
     *  page on that wiki that is linked to. For example, <tt>
     *  getInterWikiBacklinks("testwiki")</tt> may return:
     *  <pre>
     *  {
     *      { "Spam", "testwiki:Blah" },
     *      { "Test", "testwiki:Main_Page" }
     *  }
     *  </pre>
     *  <p>
     *  Here the page [[Spam]] contains the interwiki link [[testwiki:Blah]] and
     *  the page [[Test]] contains the interwiki link [[testwiki:Main_Page]].
     *  This does not resolve nested interwiki prefixes, e.g. [[wikt:fr:Test]].
     *
     *  <p>
     *  For WMF wikis, see <a href="https://meta.wikimedia.org/wiki/Interwiki_map">
     *  the interwiki map</a>for where some prefixes link to.
     *
     *  @param prefix the interwiki prefix that denotes a wiki
     *  @return all pages that contain interwiki links to said wiki
     *  @throws IOException if a network error occurs
     *  @since 0.23
     */
    public String[][] getInterWikiBacklinks(String prefix) throws IOException
    {
        return getInterWikiBacklinks(prefix, "|");
    }

    /**
     *  Fetches all pages that use interwiki links with a certain <tt>prefix</tt>
     *  and <tt>title</tt>. <tt>prefix</tt> refers to the wiki being linked to
     *  and <tt>title</tt> refers to the page on said wiki being linked to. In
     *  wiki syntax, this is [[prefix:title]]. This does not resolve nested
     *  prefixes, e.g. [[wikt:fr:Test]].
     *
     *  <p>
     *  Example: If [[Test]] and [[Spam]] both contain the interwiki link
     *  [[testwiki:Blah]] then <tt>getInterWikiBacklinks("testwiki", "Blah");
     *  </tt> will return (sorted by <tt>title</tt>)
     *  <pre>
     *  {
     *      { "Spam", "testwiki:Blah" },
     *      { "Test", "testwiki:Blah" }
     *  }
     *  </pre>
     *
     *  <p>
     *  For WMF wikis, see <a href="https://meta.wikimedia.org/wiki/Interwiki_map">
     *  the interwiki map</a>for where some prefixes link to.
     *
     *  @param prefix the interwiki prefix to search
     *  @param title the title of the page on the other wiki to search for
     *  (optional, use "|" to not specify one). Warning: "" is a valid interwiki
     *  target!
     *  @return a list of all pages that use interwiki links satisfying the
     *  parameters given
     *  @throws IOException if a network error occurs
     *  @throws IllegalArgumentException if a title is specified without a
     *  prefix (the MediaWiki API doesn't like this)
     *  @since 0.23
     */
    public String[][] getInterWikiBacklinks(String prefix, String title) throws IOException
    {
        // must specify a prefix
        if (title.equals("|") && prefix.isEmpty())
            throw new IllegalArgumentException("Interwiki backlinks: title specified without prefix!");

        StringBuilder url = new StringBuilder(query);
        url.append("list=iwbacklinks&iwbllimit=max&iwblprefix=");
        url.append(prefix);
        if (!title.equals("|"))
        {
            url.append("&iwbltitle=");
            url.append(title);
        }
        url.append("&iwblprop=iwtitle%7Ciwprefix");

        String iwblcontinue = "";
        List<String[]> links = new ArrayList<>(500);
        do
        {
            String line;
            if (iwblcontinue.isEmpty())
                line = fetch(url.toString(), "getInterWikiBacklinks");
            else
                line = fetch(url.toString() + "&iwblcontinue=" + iwblcontinue, "getInterWikiBacklinks");
            iwblcontinue = parseAttribute(line, "iwblcontinue", 0);

            // xml form: <iw pageid="24163544" ns="0" title="Elisabeth_of_Wroclaw" iwprefix="pl" iwtitle="Main_Page" />
            for (int x = line.indexOf("<iw "); x > 0; x = line.indexOf("<iw ", ++x))
            {
                links.add(new String[]
                {
                    parseAttribute(line, "title", x),
                    parseAttribute(line, "iwprefix", x) + ':' + parseAttribute(line, "iwtitle", x)
                });
            }
        }
        while (iwblcontinue != null);
        log(Level.INFO, "getInterWikiBacklinks", "Successfully retrieved interwiki backlinks (" + links.size() + " interwikis)");
        return links.toArray(new String[0][0]);
    }

    // INNER CLASSES

    /**
     *  Subclass for wiki users.
     *  @since 0.05
     */
    public class User implements Cloneable, Serializable
    {
        private String username;
        private String[] rights = null; // cache
        private String[] groups = null; // cache

        /**
         *  Creates a new user object. Does not create a new user on the
         *  wiki (we don't implement this for a very good reason). Shouldn't
         *  be called for anons.
         *
         *  @param username the username of the user
         *  @since 0.05
         */
        protected User(String username)
        {
            this.username = username;
        }

        /**
         *  Gets this user's username.
         *  @return this user's username
         *  @since 0.08
         */
        public String getUsername()
        {
            return username;
        }

        /**
         *  Gets various properties of this user. Groups and rights are cached
         *  for the current logged in user. Returns:
         *  <ul>
         *  <li><b>editcount</b>: (int) {@link #countEdits()} the user's edit
         *    count
         *  <li><b>groups</b>: (String[]) the groups the user is in (see
         *    [[Special:Listgrouprights]])
         *  <li><b>rights</b>: (String[]) the stuff the user can do
         *  <li><b>emailable</b>: (Boolean) whether the user can be emailed
         *    through [[Special:Emailuser]] or emailUser()
         *  <li><b>blocked</b>: (Boolean) whether the user is blocked
         *  <li><b>gender</b>: (Wiki.Gender) the user's gender
         *  <li><b>created</b>: (Calendar) when the user account was created
         *  </ul>
         *
         *  @return (see above)
         *  @throws IOException if a network error occurs
         *  @since 0.24
         */
        public Map<String, Object> getUserInfo() throws IOException
        {
            String info = fetch(query + "list=users&usprop=editcount%7Cgroups%7Crights%7Cemailable%7Cblockinfo%7Cgender%7Cregistration&ususers="
                + encode(username, false), "getUserInfo");
            Map<String, Object> ret = new HashMap<>(10);

            ret.put("blocked", info.contains("blockedby=\""));
            ret.put("emailable", info.contains("emailable=\""));
            ret.put("editcount", Integer.parseInt(parseAttribute(info, "editcount", 0)));
            ret.put("gender", Gender.valueOf(parseAttribute(info, "gender", 0)));

            String registrationdate = parseAttribute(info, "registration", 0);
            // TODO remove check when https://phabricator.wikimedia.org/T24097 is resolved
            if (registrationdate != null && !registrationdate.isEmpty())
                ret.put("created", timestampToCalendar(registrationdate, true));

            // groups
            List<String> temp = new ArrayList<>();
            for (int x = info.indexOf("<g>"); x > 0; x = info.indexOf("<g>", ++x))
            {
                int y = info.indexOf("</g>", x);
                temp.add(info.substring(x + 3, y));
            }
            String[] temp2 = temp.toArray(new String[temp.size()]);
            // cache
            if (this.equals(getCurrentUser()))
                groups = temp2;
            ret.put("groups", temp2);

            // rights
            temp.clear();
            for (int x = info.indexOf("<r>"); x > 0; x = info.indexOf("<r>", ++x))
            {
                int y = info.indexOf("</r>", x);
                temp.add(info.substring(x + 3, y));
            }
            temp2 = temp.toArray(new String[temp.size()]);
            // cache
            if (this.equals(getCurrentUser()))
                rights = temp2;
            ret.put("rights", temp2);
            return ret;
        }

        /**
         *  Returns true if the user is allowed to perform the specified action.
         *  Uses the rights cache. Read [[Special:Listgrouprights]] before using
         *  this!
         *  @param right a specific action
         *  @return whether the user is allowed to execute it
         *  @since 0.24
         *  @throws IOException if a network error occurs
         */
        public boolean isAllowedTo(String right) throws IOException
        {
            // We can safely assume the user is allowed to { read, edit, create,
            // writeapi }.
            if (rights == null)
                rights = (String[])getUserInfo().get("rights");
            for (String r : rights)
                if (r.equals(right))
                    return true;
            return false;
        }

        /**
         *  Returns true if the user is a member of the specified group. Uses
         *  the groups cache.
         *  @param group a specific group
         *  @return whether the user is in it
         *  @since 0.24
         *  @throws IOException if a network error occurs
         */
        public boolean isA(String group) throws IOException
        {
            if (groups == null)
                groups = (String[])getUserInfo().get("groups");
            for (String g : groups)
                if (g.equals(group))
                    return true;
            return false;
        }

        /**
         *  Returns a log of the times when the user has been blocked.
         *  @return records of the occasions when this user has been blocked
         *  @throws IOException if something goes wrong
         *  @since 0.08
         */
        public LogEntry[] blockLog() throws IOException
        {
            return getLogEntries(null, null, Integer.MAX_VALUE, BLOCK_LOG, "", null, "User:" + username, USER_NAMESPACE);
        }

        /**
         *  Determines whether this user is blocked by looking it up on the IP
         *  block list.
         *  @return whether this user is blocked
         *  @throws IOException if we cannot retrieve the IP block list
         *  @since 0.12
         */
        public boolean isBlocked() throws IOException
        {
            // @revised 0.18 now check for errors after each edit, including blocks
            return getIPBlockList(username, null, null).length != 0;
        }

        /**
         *  Fetches the internal edit count for this user, which includes all
         *  live edits and deleted edits after (I think) January 2007. If you
         *  want to count live edits only, use the slower
         *  <tt>int count = user.contribs().length;</tt>.
         *
         *  @return the user's edit count
         *  @throws IOException if a network error occurs
         *  @since 0.16
         */
        public int countEdits() throws IOException
        {
            return (Integer)getUserInfo().get("editcount");
        }

        /**
         *  Fetches the contributions for this user in a particular namespace(s).
         *  @param ns a list of namespaces to filter by, empty = all namespaces.
         *  @return a revision array of contributions
         *  @throws IOException if a network error occurs
         *  @since 0.17
         */
        public Revision[] contribs(int... ns) throws IOException
        {
            return Wiki.this.contribs(username, ns);
        }

        /**
         *  Copies this user object.
         *  @return the copy
         *  @since 0.08
         */
        @Override
        public User clone()
        {
            try
            {
                return (User)super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                return null;
            }
        }

        /**
         *  Tests whether this user is equal to another one.
         *  @param x another object
         *  @return whether the usernames of the users are equal
         *  @since 0.08
         */
        @Override
        public boolean equals(Object x)
        {
            return x instanceof User && username.equals(((User)x).username);
        }

        /**
         *  Returns a string representation of this user.
         *  @return see above
         *  @since 0.17
         */
        @Override
        public String toString()
        {
            StringBuilder temp = new StringBuilder("User[username=");
            temp.append(username);
            temp.append("groups=");
            temp.append(groups != null ? Arrays.toString(groups) : "unset");
            temp.append("]");
            return temp.toString();
        }

        /**
         *  Returns a hashcode of this user.
         *  @return see above
         *  @since 0.19
         */
        @Override
        public int hashCode()
        {
            return username.hashCode() * 2 + 1;
        }
    }

    /**
     *  A wrapper class for an entry in a wiki log, which represents an action
     *  performed on the wiki.
     *
     *  @see #getLogEntries
     *  @since 0.08
     */
    public class LogEntry implements Comparable<LogEntry>
    {
        // internal data storage
        private String type;
        private String action;
        private String reason;
        private User user;
        private String target;
        private Calendar timestamp;
        private Object details;

        /**
         *  Creates a new log entry. WARNING: does not perform the action
         *  implied. Use Wiki.class methods to achieve this.
         *
         *  @param type the type of log entry, one of USER_CREATION_LOG,
         *  DELETION_LOG, BLOCK_LOG, etc.
         *  @param action the type of action that was performed e.g. "delete",
         *  "unblock", "overwrite", etc.
         *  @param reason why the action was performed
         *  @param user the user who performed the action
         *  @param target the target of the action
         *  @param timestamp the local time when the action was performed.
         *  We will convert this back into a Calendar.
         *  @param details the details of the action (e.g. the new title of
         *  the page after a move was performed).
         *  @since 0.08
         */
        protected LogEntry(String type, String action, String reason, User user, String target, String timestamp, Object details)
        {
            this.type = type;
            this.action = action;
            this.reason = reason;
            this.user = user;
            this.target = target;
            this.timestamp = timestampToCalendar(timestamp, false);
            this.details = details;
        }

        /**
         *  Gets the type of log that this entry is in.
         *  @return one of DELETION_LOG, USER_CREATION_LOG, BLOCK_LOG, etc.
         *  @since 0.08
         */
        public String getType()
        {
            return type;
        }

        /**
         *  Gets a string description of the action performed, for example
         *  "delete", "protect", "overwrite", ... WARNING: returns null if the
         *  action was RevisionDeleted.
         *  @return the type of action performed
         *  @since 0.08
         */
        public String getAction()
        {
            return action;
        }

        /**
         *  Gets the reason supplied by the perfoming user when the action
         *  was performed. WARNING: returns null if the reason was
         *  RevisionDeleted.
         *  @return the reason the action was performed
         *  @since 0.08
         */
        public String getReason()
        {
            return reason;
        }

        /**
         *  Gets the user object representing who performed the action.
         *  WARNING: returns null if the user was RevisionDeleted.
         *  @return the user who performed the action.
         *  @since 0.08
         */
        public User getUser()
        {
            return user;
        }

        /**
         *  Gets the target of the action represented by this log entry. WARNING:
         *  returns null if the content was RevisionDeleted.
         *  @return the target of this log entry
         *  @since 0.08
         */
        public String getTarget()
        {
            return target;
        }

        /**
         *  Gets the timestamp of this log entry.
         *  @return the timestamp of this log entry
         *  @since 0.08
         */
        public Calendar getTimestamp()
        {
            return timestamp;
        }

        /**
         *  Gets the details of this log entry. Return values are as follows:
         *
         *  <table>
         *  <caption>Log types and return values</caption>
         *  <tr><th>Log type <th>Return value
         *  <tr><td>MOVE_LOG
         *      <td>The new page title
         *  <tr><td>USER_RENAME_LOG
         *      <td>The new username
         *  <tr><td>BLOCK_LOG
         *      <td>new Object[] { boolean anononly, boolean nocreate, boolean 
         *      noautoblock, boolean noemail, boolean nousertalk, String duration }
         *  <tr><td>USER_RIGHTS_LOG
         *      <td>The new user rights (String[])
         *  <tr><td>PROTECTION_LOG
         *      <td>if action == "protect" or "modify" return the protection level
         *          (int, -2 if unrecognized) if action == "move_prot" return 
         *          the old title, else null
         *  <tr><td>Others or RevisionDeleted
         *      <td>null
         *  </table>
         *
         *  Note that the duration of a block may be given as a period of time
         *  (e.g. "31 hours") or a timestamp (e.g. 20071216160302). To tell
         *  these apart, feed it into <tt>Long.parseLong()</tt> and catch any
         *  resulting exceptions.
         *
         *  @return the details of the log entry
         *  @since 0.08
         */
        public Object getDetails()
        {
            return details;
        }

        /**
         *  Returns a string representation of this log entry.
         *  @return a string representation of this object
         *  @since 0.08
         */
        @Override
        public String toString()
        {
            // @revised 0.17 to a more traditional Java approach
            StringBuilder s = new StringBuilder("LogEntry[type=");
            s.append(type);
            s.append(",action=");
            s.append(action == null ? "[hidden]" : action);
            s.append(",user=");
            s.append(user == null ? "[hidden]" : user.getUsername());
            s.append(",timestamp=");
            s.append(calendarToTimestamp(timestamp));
            s.append(",target=");
            s.append(target == null ? "[hidden]" : target);
            s.append(",reason=\"");
            s.append(reason == null ? "[hidden]" : reason);
            s.append("\",details=");
            if (details instanceof Object[])
                s.append(Arrays.asList((Object[])details)); // crude formatting hack
            else
                s.append(details);
            s.append("]");
            return s.toString();
        }

        /**
         *  Compares this log entry to another one based on the recentness
         *  of their timestamps.
         *  @param other the log entry to compare
         *  @return whether this object is equal to
         *  @since 0.18
         */
        @Override
        public int compareTo(Wiki.LogEntry other)
        {
            if (timestamp.equals(other.timestamp))
                return 0; // might not happen, but
            return timestamp.after(other.timestamp) ? 1 : -1;
        }
    }

    /**
     *  Represents a contribution and/or a revision to a page.
     *  @since 0.17
     */
    public class Revision implements Comparable<Revision>
    {
        private boolean minor, bot, rvnew;
        private String summary;
        private long revid, rcid = -1;
        private long previous = 0, next = 0;
        private Calendar timestamp;
        private String user;
        private String title;
        private String rollbacktoken = null;
        private int size = 0;
        private int sizediff = 0;
        private boolean summaryDeleted = false, userDeleted = false, contentDeleted = false;
        private boolean pageDeleted = false;

        /**
         *  Constructs a new Revision object.
         *  @param revid the id of the revision (this is a long since
         *  {{NUMBEROFEDITS}} on en.wikipedia.org is now (January 2012) ~25%
         *  of <tt>Integer.MAX_VALUE</tt>
         *  @param timestamp when this revision was made
         *  @param title the concerned article
         *  @param summary the edit summary
         *  @param user the user making this revision (may be anonymous, if not
         *  use <tt>User.getUsername()</tt>)
         *  @param minor whether this was a minor edit
         *  @param bot whether this was a bot edit
         *  @param rvnew whether this revision created a new page
         *  @param size the size of the revision
         *  @since 0.17
         */
        public Revision(long revid, Calendar timestamp, String title, String summary, String user,
            boolean minor, boolean bot, boolean rvnew, int size)
        {
            this.revid = revid;
            this.timestamp = timestamp;
            this.summary = summary;
            this.minor = minor;
            this.user = user;
            this.title = title;
            this.bot = bot;
            this.rvnew = rvnew;
            this.size = size;
        }

        /**
         *  Fetches the contents of this revision.
         *  @return the contents of the appropriate article at <tt>timestamp</tt>
         *  @throws IOException if a network error occurs
         *  @throws IllegalArgumentException if page == Special:Log/xxx.
         *  @since 0.17
         */
        public String getText() throws IOException
        {
            // logs have no content
            if (revid < 1L)
                throw new IllegalArgumentException("Log entries have no valid content!");

            String temp;
            if (pageDeleted)
            {
                String url = query + "prop=deletedrevisions&drvprop=content&revids=" + revid;
                temp = fetch(url, "Revision.getText");
                int a = temp.indexOf("<rev ");
                a = temp.indexOf(">", a) + 1;
                int b = temp.indexOf("</rev>", a);
                log(Level.INFO, "Revision.getText", "Successfully retrieved text of revision " + revid);
                return temp.substring(a, b);
            }
            else
            {
                String url = base + encode(title, true) + "&oldid=" + revid + "&action=raw";
                temp = fetch(url, "Revision.getText");
                log(Level.INFO, "Revision.getText", "Successfully retrieved text of revision " + revid);
                return temp;
            }
        }

        /**
         *  Gets the rendered text of this revision.
         *  @return the rendered contents of the appropriate article at
         *  <tt>timestamp</tt>
         *  @throws IOException if a network error occurs
         *  @throws IllegalArgumentException if page == Special:Log/xxx.
         *  @since 0.17
         */
        public String getRenderedText() throws IOException
        {
            // logs have no content
            if (revid < 1L)
                throw new IllegalArgumentException("Log entries have no valid content!");

            String temp;
            if (pageDeleted)
            {
                String url = query + "prop=deletedrevisions&drvprop=content&drvparse=1&revids=" + revid;
                temp = fetch(url, "Revision.getRenderedText");
                // TODO
            }
            else
            {
                String url = base + "&action=render&oldid=" + revid;
                temp = fetch(url, "Revision.getRenderedText");
            }
            log(Level.INFO, "Revision.getRenderedText", "Successfully retrieved rendered text of revision " + revid);
            return decode(temp);
        }

        /**
         *  Returns true if the revision content is RevisionDeleted. WARNING:
         *  the return value is meaningless until <tt>getRenderedText</tt>,
         *  <tt>getText</tt> or <tt>diff()</tt> is called because of MW api
         *  limitations.
         *
         *  @return (see above)
         *  @since 0.31
         */
        public boolean isContentDeleted()
        {
            // not currently functional
            return contentDeleted;
        }

        /**
         *  Returns a HTML rendered diff table; see the table at the <a
         *  href="https://en.wikipedia.org/w/index.php?diff=343490272">example</a>.
         *  @param other another revision on the same page.
         *  @return the difference between this and the other revision
         *  @throws IOException if a network error occurs
         *  @since 0.21
         */
        public String diff(Revision other) throws IOException
        {
            return diff(other.revid, "");
        }

        /**
         *  Returns a HTML rendered diff table between this revision and the
         *  given text. Useful for emulating the "show changes" functionality.
         *  See the table at the <a
         *  href="https://en.wikipedia.org/w/index.php?diff=343490272">example</a>.
         *  @param text some wikitext
         *  @return the difference between this and the the text provided
         *  @throws IOException if a network error occurs
         *  @since 0.21
         */
        public String diff(String text) throws IOException
        {
            return diff(0L, text);
        }

        /**
         *  Returns a HTML rendered diff table; see the table at the <a
         *  href="https://en.wikipedia.org/w/index.php?diff=343490272">example</a>.
         *  @param oldid the oldid of a revision on the same page. NEXT_REVISION,
         *  PREVIOUS_REVISION and CURRENT_REVISION can be used here for obvious
         *  effect.
         *  @return the difference between this and the other revision
         *  @throws IOException if a network error occurs
         *  @since 0.26
         */
        public String diff(long oldid) throws IOException
        {
            return diff(oldid, "");
        }

        /**
         *  Fetches a HTML rendered diff table; see the table at the <a
         *  href="https://en.wikipedia.org/w/index.php?diff=343490272">example</a>.
         *  @param oldid the id of another revision; (exclusive) or
         *  @param text some wikitext to compare against
         *  @return a difference between oldid or text or null if there is no
         *  diff (example: 586849481).
         *  @throws IOException if a network error occurs
         *  @since 0.21
         */
        protected String diff(long oldid, String text) throws IOException
        {
            if (pageDeleted)
            {
                // TODO
                return null;
            }
            else
            {
                // send via POST
                StringBuilder temp = new StringBuilder("revids=");
                temp.append(revid);
                // no switch for longs? WTF?
                if (oldid == NEXT_REVISION)
                    temp.append("&rvdiffto=next");
                else if (oldid == CURRENT_REVISION)
                    temp.append("&rvdiffto=cur");
                else if (oldid == PREVIOUS_REVISION)
                    temp.append("&rvdiffto=prev");
                else if (oldid == 0L)
                {
                    temp.append("&rvdifftotext=");
                    temp.append(text);
                }
                else
                {
                    temp.append("&rvdiffto=");
                    temp.append(oldid);
                }
                String line = post(query + "prop=revisions", temp.toString(), "Revision.diff");
                // strip extraneous information
                if (line.contains("</diff>"))
                {
                    int a = line.indexOf("<diff");
                    a = line.indexOf(">", a) + 1;
                    int b = line.indexOf("</diff>", a);
                    return decode(line.substring(a, b));
                }
                else 
                    // <diff> tag has no content if there is no diff
                    return null;
            }
        }

        /**
         *  Determines whether this Revision is equal to another object.
         *  @param o an object
         *  @return whether o is equal to this object
         *  @since 0.17
         */
        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Revision))
                return false;
            return revid == ((Revision)o).revid;
        }

        /**
         *  Returns a hash code of this revision.
         *  @return a hash code
         *  @since 0.17
         */
        @Override
        public int hashCode()
        {
            return (int)revid * 2 - Wiki.this.hashCode();
        }

        /**
         *  Checks whether this edit was marked as minor. See
         *  [[Help:Minor edit]] for details.
         *
         *  @return whether this revision was marked as minor
         *  @since 0.17
         */
        public boolean isMinor()
        {
            return minor;
        }

        /**
         *  Determines whether this revision was made by a bot.
         *  @return (see above)
         *  @since 0.23
         */
        public boolean isBot()
        {
            return bot;
        }

        /**
         *  Determines whether this revision created a new page. <br>
         *  WARNING: Will return false for all revisions prior to 2007
         *  (I think?) -- this is a MediaWiki problem.<br>
         *  WARNING: Returning true does not imply this is the bottommost
         *  revision on the page due to histmerges.<br>
         *  WARNING: Not accessible through getPageHistory() -- a MW problem.
         *  @return (see above)
         *  @since 0.27
         */
        public boolean isNew()
        {
            return rvnew;
        }

        /**
         *  Returns the edit summary for this revision, or null if the summary
         *  was RevisionDeleted and you lack the necessary privileges.
         *  @return the edit summary
         *  @since 0.17
         */
        public String getSummary()
        {
            return summary;
        }

        /**
         *  Returns true if the edit summary is RevisionDeleted.
         *  @return (see above)
         *  @since 0.30
         */
        public boolean isSummaryDeleted()
        {
            return summaryDeleted;
        }

        /**
         *  Returns the user or anon who created this revision. You should
         *  pass this (if not an IP) to <tt>getUser(String)</tt> to obtain a
         *  User object. Returns null if the user was RevisionDeleted and you
         *  lack the necessary privileges.
         *  @return the user or anon
         *  @since 0.17
         */
        public String getUser()
        {
            return user;
        }

        /**
         *  Returns true if the user is RevisionDeleted.
         *  @return (see above)
         *  @since 0.30
         */
        public boolean isUserDeleted()
        {
            return userDeleted;
        }

        /**
         *  Returns true if this revision is deleted (different from revdeleted).
         *  @return (see above)
         *  @since 0.31
         */
        public boolean isPageDeleted()
        {
            return pageDeleted;
        }

        /**
         *  Returns the page to which this revision was made.
         *  @return the page
         *  @since 0.17
         */
        public String getPage()
        {
            return title;
        }

        /**
         *  Returns the oldid of this revision. Don't confuse this with
         *  <tt>rcid</tt>
         *  @return the oldid (long)
         *  @since 0.17
         */
        public long getRevid()
        {
            return revid;
        }

        /**
         *  Gets the time that this revision was made.
         *  @return the timestamp
         *  @since 0.17
         */
        public Calendar getTimestamp()
        {
            return timestamp;
        }

        /**
         *  Gets the size of this revision in bytes.
         *  @return see above
         *  @since 0.25
         */
        public int getSize()
        {
            return size;
        }

        /**
         *  Returns the change in page size caused by this revision.
         *  @return see above
         *  @since 0.28
         */
        public int getSizeDiff()
        {
            return sizediff;
        }

        /**
         *  Returns a string representation of this revision.
         *  @return see above
         *  @since 0.17
         */
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder("Revision[oldid=");
            sb.append(revid);
            sb.append(",page=\"");
            sb.append(title);
            sb.append("\",user=");
            sb.append(user == null ? "[hidden]" : user);
            sb.append(",userdeleted=");
            sb.append(userDeleted);
            sb.append(",timestamp=");
            sb.append(calendarToTimestamp(timestamp));
            sb.append(",summary=\"");
            sb.append(summary == null ? "[hidden]" : summary);
            sb.append("\",summarydeleted=");
            sb.append(summaryDeleted);
            sb.append(",minor=");
            sb.append(minor);
            sb.append(",bot=");
            sb.append(bot);
            sb.append(",size=");
            sb.append(size);
            sb.append(",rcid=");
            sb.append(rcid == -1 ? "unset" : rcid);
            sb.append(",previous=");
            sb.append(previous);
            sb.append(",next=");
            sb.append(next);
            sb.append(",rollbacktoken=");
            sb.append(rollbacktoken == null ? "null" : rollbacktoken);
            sb.append("]");
            return sb.toString();
        }

        /**
         *  Compares this revision to another revision based on the recentness
         *  of their timestamps.
         *  @param other the revision to compare
         *  @return whether this object is equal to
         *  @since 0.18
         */
        @Override
        public int compareTo(Wiki.Revision other)
        {
            if (timestamp.equals(other.timestamp))
                return 0; // might not happen, but
            return timestamp.after(other.timestamp) ? 1 : -1;
        }

        /**
         *  Gets the previous revision.
         *  @return the previous revision, or null if this is the first revision
         *  or this object was spawned via contribs().
         *  @throws IOException if a network error occurs
         *  @since 0.28
         */
        public Revision getPrevious() throws IOException
        {
            return previous == 0 ? null : getRevision(previous);
        }

        /**
         *  Gets the next revision.
         *  @return the next revision, or null if this is the last revision
         *  or this object was spawned via contribs().
         *  @throws IOException if a network error occurs
         *  @since 0.28
         */
        public Revision getNext() throws IOException
        {
            return next == 0 ? null : getRevision(next);
        }

        /**
         *  Sets the <tt>rcid</tt> of this revision, used for patrolling.
         *  This parameter is optional. This is publicly editable for
         *  subclassing.
         *  @param rcid the rcid of this revision (long)
         *  @since 0.17
         */
        public void setRcid(long rcid)
        {
            this.rcid = rcid;
        }

        /**
         *  Gets the <tt>rcid</tt> of this revision for patrolling purposes.
         *  @return the rcid of this revision (long)
         *  @since 0.17
         */
        public long getRcid()
        {
            return rcid;
        }

        /**
         *  Sets a rollback token for this revision.
         *  @param token a rollback token
         *  @since 0.24
         */
        public void setRollbackToken(String token)
        {
            rollbacktoken = token;
        }

        /**
         *  Gets the rollback token for this revision. Can be null, and often
         *  for good reasons: cannot rollback or not top revision.
         *  @return the rollback token
         *  @since 0.24
         */
        public String getRollbackToken()
        {
            return rollbacktoken;
        }

        /**
         *  Reverts this revision using the rollback method. See
         *  <tt>Wiki.rollback()</tt>.
         *  @throws IOException if a network error occurs
         *  @throws CredentialNotFoundException if not logged in or user is not
         *  an admin
         *  @throws CredentialExpiredException if cookies have expired
         *  @throws AccountLockedException if the user is blocked
         *  @since 0.19
         */
        public void rollback() throws IOException, LoginException
        {
            Wiki.this.rollback(this, false, "");
        }

        /**
         *  Reverts this revision using the rollback method. See
         *  <tt>Wiki.rollback()</tt>.
         *  @param bot mark this and the reverted revision(s) as bot edits
         *  @param reason (optional) a custom reason
         *  @throws IOException if a network error occurs
         *  @throws CredentialNotFoundException if not logged in or user is not
         *  an admin
         *  @throws CredentialExpiredException if cookies have expired
         *  @throws AccountLockedException if the user is blocked
         *  @since 0.19
         */
        public void rollback(boolean bot, String reason) throws IOException, LoginException
        {
            Wiki.this.rollback(this, bot, reason);
        }
    }

    // INTERNALS

    // miscellany

    /**
     *  A generic URL content fetcher. This is only useful for GET requests,
     *  which is almost everything that doesn't modify the wiki. Might be
     *  useful for subclasses.
     *
     *  Here we also check the database lag and wait if it exceeds
     *  <tt>maxlag</tt>, see <a href="https://mediawiki.org/wiki/Manual:Maxlag_parameter">
     *  here</a> for how this works.
     *
     *  @param url the url to fetch
     *  @param caller the caller of this method
     *  @return the content of the fetched URL
     *  @throws IOException if a network error occurs
     *  @throws AssertionError if assert=user|bot fails
     *  @since 0.18
     */
    protected String fetch(String url, String caller) throws IOException
    {
        // connect
        logurl(url, caller);
        URLConnection connection = makeConnection(url);
        connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC);
        connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC);
        setCookies(connection);
        connection.connect();
        grabCookies(connection);

        // check lag and retry
        if (checkLag(connection))
            fetch(url, caller);

        // get the text
        String temp;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
            zipped ? new GZIPInputStream(connection.getInputStream()) : connection.getInputStream(), "UTF-8")))
        {
            String line;
            StringBuilder text = new StringBuilder(100000);
            while ((line = in.readLine()) != null)
            {
                text.append(line);
                text.append("\n");
            }
            temp = text.toString();
        }
        if (temp.contains("<error code="))
        {
            // assertions
            if ((assertion & ASSERT_BOT) == ASSERT_BOT && temp.contains("error code=\"assertbotfailed\""))
                // assert !temp.contains("error code=\"assertbotfailed\"") : "Bot privileges missing or revoked, or session expired.";
                throw new AssertionError("Bot privileges missing or revoked, or session expired.");
            if ((assertion & ASSERT_USER) == ASSERT_USER && temp.contains("error code=\"assertuserfailed\""))
                // assert !temp.contains("error code=\"assertuserfailed\"") : "Session expired.";
                throw new AssertionError("Session expired.");
            // Something *really* bad happened. Most of these are self-explanatory
            // and are indicative of bugs (not necessarily in this framework) or
            // can be avoided entirely.
            if (!temp.matches("code=\"(rvnosuchsection)")) // list "good" errors here
                throw new UnknownError("MW API error. Server response was: " + temp);
        }
        return temp;
    }

    /**
     *  Does a text-only HTTP POST.
     *  @param url the url to post to
     *  @param text the text to post
     *  @param caller the caller of this method
     *  @throws IOException if a network error occurs
     *  @return the server response
     *  @see #multipartPost(java.lang.String, java.util.Map, java.lang.String)
     *  @since 0.24
     */
    protected String post(String url, String text, String caller) throws IOException
    {
        logurl(url, caller);
        URLConnection connection = makeConnection(url);
        setCookies(connection);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC);
        connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC);
        connection.connect();

        try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"))
        {
            out.write(text);
        }
        // check lag and retry
        if (checkLag(connection))
            post(url, text, caller);
        
        StringBuilder temp = new StringBuilder(100000);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
            zipped ? new GZIPInputStream(connection.getInputStream()) : connection.getInputStream(), "UTF-8")))
        {
            grabCookies(connection);
            String line;
            while ((line = in.readLine()) != null)
            {
                temp.append(line);
                temp.append("\n");
            }
        }
        return temp.toString();
    }

    /**
     *  Performs a multi-part HTTP POST.
     *  @param url the url to post to
     *  @param params the POST parameters. Supported types: UTF-8 text, byte[].
     *  Text and parameter names must NOT be URL encoded.
     *  @param caller the caller of this method
     *  @return the server response
     *  @throws IOException if a network error occurs
     *  @see #post(java.lang.String, java.lang.String, java.lang.String)
     *  @see <a href="http://www.w3.org/TR/html4/interact/forms.html#h-17.13.4.2">Multipart/form-data</a>
     *  @since 0.27
     */
    protected String multipartPost(String url, Map<String, ?> params, String caller) throws IOException
    {
        // set up the POST
        logurl(url, caller);
        URLConnection connection = makeConnection(url);
        String boundary = "----------NEXT PART----------";
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        setCookies(connection);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC);
        connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC);
        connection.connect();
        
        // write stuff to a local buffer
        boundary = "--" + boundary + "\r\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bout))
        {
            out.writeBytes(boundary);

            // write params
            for (Map.Entry<String, ?> entry : params.entrySet())
            {
                String name = entry.getKey();
                Object value = entry.getValue();
                out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
                if (value instanceof String)
                {
                    out.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
                    out.write(((String)value).getBytes("UTF-8"));
                }
                else if (value instanceof byte[])
                {
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                    out.write((byte[])value);
                }
                else
                    throw new UnsupportedOperationException("Unrecognized data type");
                out.writeBytes("\r\n");
                out.writeBytes(boundary);
            }
            out.writeBytes("--\r\n");
        }
        try (OutputStream uout = connection.getOutputStream())
        {
            // write the buffer to the URLConnection
            uout.write(bout.toByteArray());
        }
        
        // check lag and retry
        if (checkLag(connection))
            multipartPost(url, params, caller);

        // done, read the response
        StringBuilder temp = new StringBuilder(100000);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
            zipped ? new GZIPInputStream(connection.getInputStream()) : connection.getInputStream(), "UTF-8")))
        {
            grabCookies(connection);
            String line;
            while ((line = in.readLine()) != null)
            {
                temp.append(line);
                temp.append("\n");
            }
        }
        return temp.toString();
    }
    
    /**
     *  Checks for database lag and sleeps if lag &gt; maxlag.
     *  @param connection the URL connection used in the request
     *  @return true if there was sufficient database lag.
     *  @since 0.32
     */
    protected synchronized boolean checkLag(URLConnection connection)
    {
        // check lag
        int lag = connection.getHeaderFieldInt("X-Database-Lag", -5);
        if (lag > maxlag)
        {
            try
            {
                int time = connection.getHeaderFieldInt("Retry-After", 10);
                logger.log(Level.WARNING, "Current database lag {0} s exceeds {1} s, waiting {2} s.", new Object[] { lag, maxlag, time });
                Thread.sleep(time * 1000);
            }
            catch (InterruptedException ex)
            {
                // nobody cares
            }
            return true;
        }
        return false;
    }

    /**
     *  Creates a new URL connection. Override to change SSL handling, use a
     *  proxy, etc.
     *  @param url a URL string
     *  @return a connection to that URL
     *  @throws IOException if a network error occurs
     *  @since 0.31
     */
    protected URLConnection makeConnection(String url) throws IOException
    {
        return new URL(url).openConnection();
    }

    /**
     *  Checks for errors from standard read/write requests and performs
     *  occasional status checks.
     *
     *  @param line the response from the server to analyze
     *  @param caller what we tried to do
     *  @throws CredentialNotFoundException if permission denied
     *  @throws AccountLockedException if the user is blocked
     *  @throws HttpRetryException if the database is locked or action was
     *  throttled and a retry failed
     *  @throws AssertionError if assertions fail
     *  @throws UnknownError in the case of a MediaWiki bug
     *  @since 0.18
     */
    protected void checkErrorsAndUpdateStatus(String line, String caller) throws IOException, LoginException
    {
        // perform various status checks every 100 or so edits
        if (statuscounter > statusinterval)
        {
            // purge user rights in case of desysop or loss of other priviliges
            user.getUserInfo();
            if ((assertion & ASSERT_SYSOP) == ASSERT_SYSOP && !user.isA("sysop"))
                // assert user.isA("sysop") : "Sysop privileges missing or revoked, or session expired";
                throw new AssertionError("Sysop privileges missing or revoked, or session expired");
            // check for new messages
            if ((assertion & ASSERT_NO_MESSAGES) == ASSERT_NO_MESSAGES && hasNewMessages())
                // assert !hasNewMessages() : "User has new messages";
                throw new AssertionError("User has new messages");
            statuscounter = 0;
        }
        else
            statuscounter++;

        // successful
        if (line.contains("result=\"Success\""))
            return;
        // empty response from server
        if (line.isEmpty())
            throw new UnknownError("Received empty response from server!");
        // assertions
        if ((assertion & ASSERT_BOT) == ASSERT_BOT && line.contains("error code=\"assertbotfailed\""))
            // assert !line.contains("error code=\"assertbotfailed\"") : "Bot privileges missing or revoked, or session expired.";
            throw new AssertionError("Bot privileges missing or revoked, or session expired.");
        if ((assertion & ASSERT_USER) == ASSERT_USER && line.contains("error code=\"assertuserfailed\""))
            // assert !line.contains("error code=\"assertuserfailed\"") : "Session expired.";
            throw new AssertionError("Session expired.");
        // protected page errors
        if (line.matches("(protectednamespace|customcssjsprotected|cascadeprotected|protectedpage|protectedtitle)"))
            throw new CredentialNotFoundException("Page is protected.");
        if (line.contains("error code=\"permissiondenied\""))
            throw new CredentialNotFoundException("Permission denied."); // session expired or stupidity
        // rate limit (automatic retry), though might be a long one (e.g. email)
        if (line.contains("error code=\"ratelimited\""))
        {
            log(Level.WARNING, caller, "Server-side throttle hit.");
            throw new HttpRetryException("Action throttled.", 503);
        }
        // blocked! (note here the \" in blocked is deliberately missing for emailUser()
        if (line.contains("error code=\"blocked") || line.contains("error code=\"autoblocked\""))
        {
            log(Level.SEVERE, caller, "Cannot " + caller + " - user is blocked!.");
            throw new AccountLockedException("Current user is blocked!");
        }
        // database lock (automatic retry)
        if (line.contains("error code=\"readonly\""))
        {
            log(Level.WARNING, caller, "Database locked!");
            throw new HttpRetryException("Database locked!", 503);
        }
        // unknown error
        if (line.contains("error code=\"unknownerror\""))
            throw new UnknownError("Unknown MediaWiki API error, response was " + line);
        // generic (automatic retry)
        throw new IOException("MediaWiki error, response was " + line);
    }

    /**
     *  Strips entity references like &quot; from the supplied string. This
     *  might be useful for subclasses.
     *  @param in the string to remove URL encoding from
     *  @return that string without URL encoding
     *  @since 0.11
     */
    protected String decode(String in)
    {
        // Remove entity references. Oddly enough, URLDecoder doesn't nuke these.
        in = in.replace("&lt;", "<").replace("&gt;", ">"); // html tags
        in = in.replace("&quot;", "\"");
        in = in.replace("&#039;", "'");
        in = in.replace("&amp;", "&");
        return in;
    }

    /**
     *  Parses the next XML attribute with the given name.
     *  @param xml the xml to search
     *  @param attribute the attribute to search
     *  @param index where to start looking
     *  @return the value of the given XML attribute, or null if the attribute
     *  is not present
     *  @since 0.28
     */
    protected String parseAttribute(String xml, String attribute, int index)
    {
        // let's hope the JVM always inlines this
        if (xml.contains(attribute + "=\""))
        {
            int a = xml.indexOf(attribute + "=\"", index) + attribute.length() + 2;
            int b = xml.indexOf('\"', a);
            return decode(xml.substring(a, b));
        }
        else
            return null;
    }

    /**
     *  Convenience method for converting a namespace list into String form.
     *  @param sb the url StringBuilder to append to
     *  @param id the request type prefix (e.g. "pl" for prop=links)
     *  @param namespaces the list of namespaces to append
     *  @since 0.27
     */
    protected void constructNamespaceString(StringBuilder sb, String id, int... namespaces)
    {
        int temp = namespaces.length;
        if (temp == 0)
            return;
        sb.append("&");
        sb.append(id);
        sb.append("namespace=");
        for (int i = 0; i < temp - 1; i++)
        {
            sb.append(namespaces[i]);
            sb.append("%7C");
        }
        sb.append(namespaces[temp - 1]);
        
        // JDK 1.8:
        // if (namespaces.length == 0)
        //     return;
        // StringJoiner sj = new StringJoiner("%7C");
        // for (namespace : namespaces)
        //     sj.add("" + namespace);
        // sb.add(sj.toString());
    }

    /**
     *  Cuts up a list of titles into batches for prop=X&amp;titles=Y type queries.
     *  @param titles a list of titles.
     *  @return the titles ready for insertion into a URL
     *  @throws IOException if a network error occurs
     *  @since 0.29
     */
    protected String[] constructTitleString(String[] titles) throws IOException
    {
        // remove duplicates, sort and pad
        // Set<String> set = new TreeSet(Arrays.asList(titles));
        // String[] temp = set.toArray(new String[set.size()]);
        // String[] aaa = new String[titles.length];
        // System.arraycopy(temp, 0, titles, 0, temp.length);
        // System.arraycopy(aaa, 0, titles, temp.length, titles.length - temp.length);

        // actually construct the string
        String[] ret = new String[titles.length / slowmax + 1];
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < titles.length; i++)
        {
            buffer.append(normalize(titles[i]));
            if (i == titles.length - 1 || (i % slowmax == slowmax - 1))
            {
                ret[i / slowmax] = encode(buffer.toString(), false);
                buffer = new StringBuilder();
            }
            else
                buffer.append("|");
        }
        // JDK 1.8:
        // StringJoiner sj = new StringJoiner("|");
        // for (int i = 0; i < titles.length; i++)
        // {
        //     sj.add(normalize(titles[i]));
        //     statement of if above, removing else
        // }
        return ret;
    }

    /**
     * UTF-8 encode the String with URLEncoder after optional normalization;
     * Usually, normalize should be set to true when a title or name String is
     * passed in as an argument of a method.
     * 
     * @param text the text to encode
     * @param normalize if the text should be normalized first
     * @return the encoded text
     * @throws IOException if a network error occurs during initialization of the namespaces
     */
    private String encode(String text, boolean normalize) throws IOException
    {
        final String encoding = "UTF-8";
        if (normalize)
            text = normalize(text);
        return URLEncoder.encode(text, encoding);
    }

    /**
     *  Convenience method for normalizing MediaWiki titles. (Converts all
     *  underscores to spaces, localizes namespace names, fixes case of first
     *  char and does some other unicode fixes).
     *  @param s the string to normalize
     *  @return the normalized string
     *  @throws IllegalArgumentException if the title is invalid
     *  @throws IOException if a network error occurs during initialization of the namespaces
     *  @since 0.27
     */
    public String normalize(String s) throws IOException
    {
        // remove leading colon
        if (s.startsWith(":"))
            s = s.substring(1);
        if (s.isEmpty())
            return s;

        int ns = namespace(s);
        // localize namespace names
        if (ns != MAIN_NAMESPACE)
        {
            int colon = s.indexOf(":");
            s = namespaceIdentifier(ns) + s.substring(colon);
        }
        char[] temp = s.toCharArray();
        if (wgCapitalLinks)
        {
            // convert first character in the actual title to upper case
            if (ns == MAIN_NAMESPACE)
                temp[0] = Character.toUpperCase(temp[0]);
            else
            {
                int index = namespaceIdentifier(ns).length() + 1; // + 1 for colon
                temp[index] = Character.toUpperCase(temp[index]);
            }
        }

        for (int i = 0; i < temp.length; i++)
        {
            switch (temp[i])
            {
            // illegal characters
                case '{':
                case '}':
                case '<':
                case '>':
                case '[':
                case ']':
                case '|':
                    throw new IllegalArgumentException(s + " is an illegal title");
                case '_':
                    temp[i] = ' ';
                    break;
            }
        }
        // https://www.mediawiki.org/wiki/Unicode_normalization_considerations
        String temp2 = new String(temp).trim().replaceAll("\\s+", " ");
        return Normalizer.normalize(temp2, Normalizer.Form.NFC);
    }

    /**
     *  Ensures no less than <tt>throttle</tt> milliseconds pass between edits
     *  and other write actions.
     *  @since 0.30
     */
    private synchronized void throttle()
    {
        try
        {
            long time = throttle - System.currentTimeMillis() + lastThrottleActionTime;
            if (time > 0)
                Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            // nobody cares
        }
        this.lastThrottleActionTime = System.currentTimeMillis();
    }

    // user rights methods

    /**
     *  Checks whether the currently logged on user has sufficient rights to
     *  edit/move a protected page.
     *
     *  @param pageinfo the output from <tt>getPageInfo()</tt>
     *  @param action what we are doing
     *  @return whether the user can perform the specified action
     *  @throws IOException if a network error occurs
     *  @since 0.10
     */
    protected boolean checkRights(Map<String, Object> pageinfo, String action) throws IOException
    {
        Map<String, Object> protectionstate = (Map<String, Object>)pageinfo.get("protection");
        if (protectionstate.containsKey(action))
        {
            String level = (String)protectionstate.get(action);
            if (level.equals(SEMI_PROTECTION))
                return user.isAllowedTo("autoconfirmed");
            if (level.equals(FULL_PROTECTION))
                return user.isAllowedTo("editprotected");
        }
        if ((Boolean)protectionstate.get("cascade") == Boolean.TRUE) // can be null
            return user.isAllowedTo("editprotected");
        return true;
    }

    // cookie methods

    /**
     *  Sets cookies to an unconnected URLConnection and enables gzip
     *  compression of returned text.
     *  @param u an unconnected URLConnection
     */
    protected void setCookies(URLConnection u)
    {
        StringBuilder cookie = new StringBuilder(100);
        for (Map.Entry<String, String> entry : cookies.entrySet())
        {
            cookie.append(entry.getKey());
            cookie.append("=");
            cookie.append(entry.getValue());
            cookie.append("; ");
        }
        u.setRequestProperty("Cookie", cookie.toString());

        // enable gzip compression
        if (zipped)
            u.setRequestProperty("Accept-encoding", "gzip");
        u.setRequestProperty("User-Agent", useragent);
    }

    /**
     *  Grabs cookies from the URL connection provided.
     *  @param u an unconnected URLConnection
     */
    private void grabCookies(URLConnection u)
    {
        String headerName;
        for (int i = 1; (headerName = u.getHeaderFieldKey(i)) != null; i++)
            if (headerName.equals("Set-Cookie"))
            {
                String cookie = u.getHeaderField(i);
                cookie = cookie.substring(0, cookie.indexOf(';'));
                String name = cookie.substring(0, cookie.indexOf('='));
                String value = cookie.substring(cookie.indexOf('=') + 1, cookie.length());
                // these cookies were pruned, but are still sent for some reason?
                // TODO: when these cookies are no longer sent, remove this test
                if (!value.equals("deleted"))
                    cookies.put(name, value);
            }
    }

    // logging methods

    /**
     *  Logs a successful result.
     *  @param text string the string to log
     *  @param method what we are currently doing
     *  @param level the level to log at
     *  @since 0.06
     */
    protected void log(Level level, String method, String text)
    {
        logger.logp(level, "Wiki", method, "[{0}] {1}", new Object[] { domain, text });
    }

    /**
     *  Logs a url fetch.
     *  @param url the url we are fetching
     *  @param method what we are currently doing
     *  @since 0.08
     */
    protected void logurl(String url, String method)
    {
        logger.logp(Level.INFO, "Wiki", method, "Fetching URL {0}", url);
    }

    // calendar/timestamp methods

    /**
     *  Creates a Calendar object with the current time. Wikimedia wikis use UTC.
     *  @return see above
     *  @since 0.26
     */
    public Calendar makeCalendar()
    {
        return new GregorianCalendar(TimeZone.getTimeZone(timezone));
    }

    /**
     *  Turns a calendar into a timestamp of the format yyyymmddhhmmss. Might
     *  be useful for subclasses.
     *  @param c the calendar to convert
     *  @return the converted calendar
     *  @see #timestampToCalendar
     *  @since 0.08
     */
    protected String calendarToTimestamp(Calendar c)
    {
        return String.format("%04d%02d%02d%02d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }

    /**
     *  Turns a timestamp into a Calendar object. Might be useful for subclasses.
     *
     *  @param timestamp the timestamp to convert
     *  @param api whether the timestamp is of the format yyyy-mm-ddThh:mm:ssZ
     *  as opposed to yyyymmddhhmmss (which is the default)
     *  @return the converted Calendar
     *  @see #calendarToTimestamp
     *  @since 0.08
     */
    protected final Calendar timestampToCalendar(String timestamp, boolean api)
    {
        // TODO: move to Java 1.8
        Calendar calendar = makeCalendar();
        if (api)
            timestamp = convertTimestamp(timestamp);
        int year = Integer.parseInt(timestamp.substring(0, 4));
        int month = Integer.parseInt(timestamp.substring(4, 6)) - 1; // January == 0!
        int day = Integer.parseInt(timestamp.substring(6, 8));
        int hour = Integer.parseInt(timestamp.substring(8, 10));
        int minute = Integer.parseInt(timestamp.substring(10, 12));
        int second = Integer.parseInt(timestamp.substring(12, 14));
        calendar.set(year, month, day, hour, minute, second);
        return calendar;
    }

    /**
     *  Converts a timestamp of the form used by the API (yyyy-mm-ddThh:mm:ssZ)
     *  to the form yyyymmddhhmmss.
     *
     *  @param timestamp the timestamp to convert
     *  @return the converted timestamp
     *  @see #timestampToCalendar
     *  @since 0.12
     */
    protected String convertTimestamp(String timestamp)
    {
        // TODO: remove this once Java 1.8 comes around
        StringBuilder ts = new StringBuilder(timestamp.substring(0, 4));
        ts.append(timestamp.substring(5, 7));
        ts.append(timestamp.substring(8, 10));
        ts.append(timestamp.substring(11, 13));
        ts.append(timestamp.substring(14, 16));
        ts.append(timestamp.substring(17, 19));
        return ts.toString();
    }

    // serialization

    /**
     *  Writes this wiki to a file.
     *  @param out an ObjectOutputStream to write to
     *  @throws IOException if there are local IO problems
     *  @since 0.10
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    /**
     *  Reads a copy of a wiki from a file.
     *  @param in an ObjectInputStream to read from
     *  @throws IOException if there are local IO problems
     *  @throws ClassNotFoundException if we can't recognize the input
     *  @since 0.10
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        statuscounter = statusinterval; // force a status check on next edit
    }
}
