/**
 * @copyright
 * ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 * ====================================================================
 * @endcopyright
 */
package org.apache.subversion.javahl;

import org.apache.subversion.javahl.*;
import org.apache.subversion.javahl.remote.*;
import org.apache.subversion.javahl.callback.*;
import org.apache.subversion.javahl.types.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is used for testing the SVNReposAccess class
 *
 * More methodes for testing are still needed
 */
public class SVNRemoteTests extends SVNTests
{
    protected OneTest thisTest;

    public SVNRemoteTests()
    {
    }

    public SVNRemoteTests(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        thisTest = new OneTest();
    }

    public static ISVNRemote getSession(String url, String configDirectory,
                                        ConfigEvent configHandler)
    {
        try
        {
            RemoteFactory factory = new RemoteFactory();
            factory.setConfigDirectory(configDirectory);
            factory.setConfigEventHandler(configHandler);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setPrompt(new DefaultPromptUserPassword());

            ISVNRemote raSession = factory.openRemoteSession(url);
            assertNotNull("Null session was returned by factory", raSession);
            return raSession;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private ISVNRemote getSession()
    {
        return getSession(getTestRepoUrl(), super.conf.getAbsolutePath(), null);
    }

    /**
     * Test the basic SVNAdmin.create functionality
     * @throws SubversionException
     */
    public void testCreate()
        throws SubversionException, IOException
    {
        assertTrue("repository exists", thisTest.getRepository().exists());
    }

    public void testGetSession_ConfigConstructor() throws Exception
    {
        ISVNRemote session;
        try
        {
            session = new RemoteFactory(
                super.conf.getAbsolutePath(), null,
                USERNAME, PASSWORD,
                new DefaultPromptUserPassword(), null)
                .openRemoteSession(getTestRepoUrl());
        }
        catch (ClientException ex)
        {
            throw new RuntimeException(ex);
        }
        assertNotNull("Null session was returned by factory", session);
        assertEquals(getTestRepoUrl(), session.getSessionUrl());
    }

    public void testDispose() throws Exception
    {
        ISVNRemote session = getSession();
        session.dispose();
    }

    public void testDatedRev() throws Exception
    {
        ISVNRemote session = getSession();

        long revision = session.getRevisionByDate(new Date());
        assertEquals(revision, 1);
    }

    public void testGetLocks() throws Exception
    {
        ISVNRemote session = getSession();

        Set<String> iotaPathSet = new HashSet<String>(1);
        String iotaPath = thisTest.getWCPath() + "/iota";
        iotaPathSet.add(iotaPath);

        client.lock(iotaPathSet, "foo", false);

        Map<String, Lock> locks = session.getLocks("iota", Depth.infinity);

        assertEquals(locks.size(), 1);
        Lock lock = locks.get("/iota");
        assertNotNull(lock);
        assertEquals(lock.getOwner(), "jrandom");
    }

    public void testCheckPath() throws Exception
    {
        ISVNRemote session = getSession();

        NodeKind kind = session.checkPath("iota", 1);
        assertEquals(NodeKind.file, kind);

        kind = session.checkPath("iota", 0);
        assertEquals(NodeKind.none, kind);

        kind = session.checkPath("A", 1);
        assertEquals(NodeKind.dir, kind);
    }

    private String getTestRepoUrl()
    {
        return thisTest.getUrl().toASCIIString();
    }

    public void testGetLatestRevision() throws Exception
    {
        ISVNRemote session = getSession();
        long revision = session.getLatestRevision();
        assertEquals(revision, 1);
    }

    public void testGetUUID() throws Exception
    {
        ISVNRemote session = getSession();

        /*
         * Test UUID
         * TODO: Test for actual UUID once test dump file has
         * fixed UUID
         */
        assertNotNull(session.getReposUUID());
    }

    public void testGetUrl() throws Exception
    {
        ISVNRemote session = getSession();

        assertEquals(getTestRepoUrl(), session.getSessionUrl());
    }

    public void testGetRootUrl() throws Exception
    {
        ISVNRemote session = getSession();
        session.reparent(session.getSessionUrl() + "/A/B/E");
        assertEquals(getTestRepoUrl(), session.getReposRootUrl());
    }

    public void testGetUrl_viaSVNClient() throws Exception
    {
        ISVNRemote session = client.openRemoteSession(getTestRepoUrl());

        assertEquals(getTestRepoUrl(), session.getSessionUrl());
    }

    public void testGetUrl_viaSVNClientWorkingCopy() throws Exception
    {
        ISVNRemote session = client.openRemoteSession(thisTest.getWCPath());

        assertEquals(getTestRepoUrl(), session.getSessionUrl());
    }

    public void testReparent() throws Exception
    {
        ISVNRemote session = getSession();
        String newUrl = session.getSessionUrl() + "/A/B/E";
        session.reparent(newUrl);
        assertEquals(newUrl, session.getSessionUrl());
    }

    public void testGetRelativePath() throws Exception
    {
        ISVNRemote session = getSession();
        String baseUrl = session.getSessionUrl() + "/A/B/E";
        session.reparent(baseUrl);

        String relPath = session.getSessionRelativePath(baseUrl + "/alpha");
        assertEquals("alpha", relPath);

        relPath = session.getReposRelativePath(baseUrl + "/beta");
        assertEquals("A/B/E/beta", relPath);
    }

    public void testGetCommitEditor() throws Exception
    {
        ISVNRemote session = getSession();
        session.getCommitEditor(null, null, null, false);
    }

    public void testDisposeCommitEditor() throws Exception
    {
        ISVNRemote session = getSession();
        session.getCommitEditor(null, null, null, false);
        session.dispose();
    }

    public void testHasCapability() throws Exception
    {
        ISVNRemote session = getSession();
        assert(session.hasCapability(ISVNRemote.Capability.depth));
    }

    public void testChangeRevpropNoAtomic() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        boolean atomic =
            session.hasCapability(ISVNRemote.Capability.atomic_revprops);

        if (atomic)
            return;

        boolean exceptioned = false;
        try
        {
            byte[] oldValue = "bumble".getBytes(UTF8);
            byte[] newValue = "bee".getBytes(UTF8);
            session.changeRevisionProperty(1, "svn:author",
                                           oldValue, newValue);
        }
        catch (IllegalArgumentException ex)
        {
            exceptioned = true;
        }
        assert(exceptioned);
    }

    public void testChangeRevpropAtomic() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        boolean atomic =
            session.hasCapability(ISVNRemote.Capability.atomic_revprops);

        if (!atomic)
            return;

        byte[] oldValue = client.revProperty(getTestRepoUrl(), "svn:author",
                                             Revision.getInstance(1));
        byte[] newValue = "rayjandom".getBytes(UTF8);
        try
        {
            session.changeRevisionProperty(1, "svn:author",
                                           oldValue, newValue);
        }
        catch (ClientException ex)
        {
            assertEquals("Disabled repository feature",
                         ex.getAllMessages().get(0).getMessage());
            return;
        }

        byte[] check = client.revProperty(getTestRepoUrl(), "svn:author",
                                          Revision.getInstance(1));
        assertTrue(Arrays.equals(check, newValue));
    }

    public void testGetRevpropList() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        Map<String, byte[]> proplist = session.getRevisionProperties(1);
        assertTrue(Arrays.equals(proplist.get("svn:author"),
                                 USERNAME.getBytes(UTF8)));
    }

    public void testGetRevprop() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        byte[] propval = session.getRevisionProperty(1, "svn:author");
        assertTrue(Arrays.equals(propval, USERNAME.getBytes(UTF8)));
    }

    public void testGetFile() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        ByteArrayOutputStream contents = new ByteArrayOutputStream();
        HashMap<String, byte[]> properties = new HashMap<String, byte[]>();
        properties.put("fakename", "fakecontents".getBytes(UTF8));
        long fetched_rev =
            session.getFile(Revision.SVN_INVALID_REVNUM, "A/B/lambda",
                            contents, properties);
        assertEquals(fetched_rev, 1);
        assertEquals(contents.toString("UTF-8"),
                     "This is the file 'lambda'.");
        for (Map.Entry<String, byte[]> e : properties.entrySet())
            assertTrue(e.getKey().startsWith("svn:entry:"));
    }

    public void testGetDirectory() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        HashMap<String, DirEntry> dirents = new HashMap<String, DirEntry>();
        dirents.put("E", null);
        dirents.put("F", null);
        dirents.put("lambda", null);
        HashMap<String, byte[]> properties = new HashMap<String, byte[]>();
        properties.put("fakename", "fakecontents".getBytes(UTF8));
        long fetched_rev =
            session.getDirectory(Revision.SVN_INVALID_REVNUM, "A/B",
                                 DirEntry.Fields.all, dirents, properties);
        assertEquals(fetched_rev, 1);
        assertEquals(dirents.get("E").getPath(), "E");
        assertEquals(dirents.get("F").getPath(), "F");
        assertEquals(dirents.get("lambda").getPath(), "lambda");
        for (Map.Entry<String, byte[]> e : properties.entrySet())
            assertTrue(e.getKey().startsWith("svn:entry:"));
    }

    private final class CommitContext implements CommitCallback
    {
        public final ISVNEditor editor;
        public CommitContext(ISVNRemote session, String logstr)
            throws ClientException
        {
            Charset UTF8 = Charset.forName("UTF-8");
            byte[] log = (logstr == null
                          ? new byte[0]
                          : logstr.getBytes(UTF8));
            HashMap<String, byte[]> revprops = new HashMap<String, byte[]>();
            revprops.put("svn:log", log);
            editor = session.getCommitEditor(revprops, this, null, false);
        }

        public void commitInfo(CommitInfo info) { this.info = info; }
        public long getRevision() { return info.getRevision(); }

        private CommitInfo info;
    }

    public void testEditorCopy() throws Exception
    {
        ISVNRemote session = getSession();
        CommitContext cc =
            new CommitContext(session, "Copy A/B/lambda -> A/B/omega");

        try {
            // FIXME: alter dir A/B first
            cc.editor.copy("A/B/lambda", 1, "A/B/omega",
                           Revision.SVN_INVALID_REVNUM);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        assertEquals(NodeKind.file,
                     session.checkPath("A/B/lambda",
                                       Revision.SVN_INVALID_REVNUM));
        assertEquals(NodeKind.file,
                     session.checkPath("A/B/omega",
                                       Revision.SVN_INVALID_REVNUM));
    }

    public void testEditorMove() throws Exception
    {
        ISVNRemote session = getSession();
        CommitContext cc =
            new CommitContext(session, "Move A/B/lambda -> A/B/omega");

        try {
            // FIXME: alter dir A/B first
            cc.editor.move("A/B/lambda", 1, "A/B/omega",
                           Revision.SVN_INVALID_REVNUM);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        assertEquals(NodeKind.none,
                     session.checkPath("A/B/lambda",
                                       Revision.SVN_INVALID_REVNUM));
        assertEquals(NodeKind.file,
                     session.checkPath("A/B/omega",
                                       Revision.SVN_INVALID_REVNUM));
    }

    public void testEditorDelete() throws Exception
    {
        ISVNRemote session = getSession();
        CommitContext cc =
            new CommitContext(session, "Delete all greek files");

        String[] filePaths = { "iota",
                               "A/mu",
                               "A/B/lambda",
                               "A/B/E/alpha",
                               "A/B/E/beta",
                               "A/D/gamma",
                               "A/D/G/pi",
                               "A/D/G/rho",
                               "A/D/G/tau",
                               "A/D/H/chi",
                               "A/D/H/omega",
                               "A/D/H/psi" };

        try {
            // FIXME: alter a bunch of dirs first
            for (String path : filePaths)
                cc.editor.delete(path, 1);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        for (String path : filePaths)
            assertEquals(NodeKind.none,
                         session.checkPath(path, Revision.SVN_INVALID_REVNUM));
    }

    public void testEditorMkdir() throws Exception
    {
        ISVNRemote session = getSession();
        CommitContext cc = new CommitContext(session, "Make hebrew dir");

        try {
            // FIXME: alter dir . first
            cc.editor.addDirectory("ALEPH",
                                   new ArrayList<String>(),
                                   new HashMap<String, byte[]>(),
                                   Revision.SVN_INVALID_REVNUM);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        assertEquals(NodeKind.dir,
                     session.checkPath("ALEPH",
                                       Revision.SVN_INVALID_REVNUM));
    }

    public void testEditorSetDirProps() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        byte[] ignoreval = "*.pyc\n.gitignore\n".getBytes(UTF8);
        HashMap<String, byte[]> props = new HashMap<String, byte[]>();
        props.put("svn:ignore", ignoreval);

        CommitContext cc = new CommitContext(session, "Add svn:ignore");
        try {
            cc.editor.alterDirectory("", 1, null, props);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        assertTrue(Arrays.equals(ignoreval,
                                 client.propertyGet(session.getSessionUrl(),
                                                    "svn:ignore",
                                                    Revision.HEAD,
                                                    Revision.HEAD)));
    }

    private static byte[] SHA1(byte[] text) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(text);
    }

    public void testEditorAddFile() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        byte[] eolstyle = "native".getBytes(UTF8);
        HashMap<String, byte[]> props = new HashMap<String, byte[]>();
        props.put("svn:eol-style", eolstyle);

        byte[] contents = "This is file 'xi'.".getBytes(UTF8);
        Checksum hash = new Checksum(SHA1(contents), Checksum.Kind.SHA1);
        ByteArrayInputStream stream = new ByteArrayInputStream(contents);

        CommitContext cc = new CommitContext(session, "Add A/xi");
        try {
            // FIXME: alter dir A first
            cc.editor.addFile("A/xi", hash, stream, props,
                              Revision.SVN_INVALID_REVNUM);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        assertEquals(NodeKind.file,
                     session.checkPath("A/xi",
                                       Revision.SVN_INVALID_REVNUM));

        byte[] propval = client.propertyGet(session.getSessionUrl() + "/A/xi",
                                            "svn:eol-style",
                                            Revision.HEAD,
                                            Revision.HEAD);
        assertTrue(Arrays.equals(eolstyle, propval));
    }

    public void testEditorSetFileProps() throws Exception
    {
        Charset UTF8 = Charset.forName("UTF-8");
        ISVNRemote session = getSession();

        byte[] eolstyle = "CRLF".getBytes(UTF8);
        HashMap<String, byte[]> props = new HashMap<String, byte[]>();
        props.put("svn:eol-style", eolstyle);

        CommitContext cc =
            new CommitContext(session, "Change eol-style on A/B/E/alpha");
        try {
            cc.editor.alterFile("A/B/E/alpha", 1, null, null, props);
            cc.editor.complete();
        } finally {
            cc.editor.dispose();
        }

        assertEquals(2, cc.getRevision());
        assertEquals(2, session.getLatestRevision());
        byte[] propval = client.propertyGet(session.getSessionUrl()
                                            + "/A/B/E/alpha",
                                            "svn:eol-style",
                                            Revision.HEAD,
                                            Revision.HEAD);
        assertTrue(Arrays.equals(eolstyle, propval));
    }

    // public void testEditorRotate() throws Exception
    // {
    //     ISVNRemote session = getSession();
    //
    //     ArrayList<ISVNEditor.RotatePair> rotation =
    //         new ArrayList<ISVNEditor.RotatePair>(3);
    //     rotation.add(new ISVNEditor.RotatePair("A/B", 1));
    //     rotation.add(new ISVNEditor.RotatePair("A/C", 1));
    //     rotation.add(new ISVNEditor.RotatePair("A/D", 1));
    //
    //     CommitContext cc =
    //         new CommitContext(session, "Rotate A/B -> A/C -> A/D");
    //     try {
    //         // No alter-dir of A is needed, children remain the same.
    //         cc.editor.rotate(rotation);
    //         cc.editor.complete();
    //     } finally {
    //         cc.editor.dispose();
    //     }
    //
    //     assertEquals(2, cc.getRevision());
    //     assertEquals(2, session.getLatestRevision());
    //
    //     HashMap<String, DirEntry> dirents = new HashMap<String, DirEntry>();
    //     HashMap<String, byte[]> properties = new HashMap<String, byte[]>();
    //
    //     // A/B is now what used to be A/D, so A/B/H must exist
    //     session.getDirectory(Revision.SVN_INVALID_REVNUM, "A/B",
    //                          DirEntry.Fields.all, dirents, properties);
    //     assertEquals(dirents.get("H").getPath(), "H");
    //
    //     // A/C is now what used to be A/B, so A/C/F must exist
    //     session.getDirectory(Revision.SVN_INVALID_REVNUM, "A/C",
    //                          DirEntry.Fields.all, dirents, properties);
    //     assertEquals(dirents.get("F").getPath(), "F");
    //
    //     // A/D is now what used to be A/C and must be empty
    //     session.getDirectory(Revision.SVN_INVALID_REVNUM, "A/D",
    //                          DirEntry.Fields.all, dirents, properties);
    //     assertTrue(dirents.isEmpty());
    // }

    // Sanity check so that we don't forget about unimplemented methods.
    public void testEditorNotImplemented() throws Exception
    {
        ISVNRemote session = getSession();

        HashMap<String, byte[]> props = new HashMap<String, byte[]>();
        // ArrayList<ISVNEditor.RotatePair> rotation =
        //     new ArrayList<ISVNEditor.RotatePair>();

        CommitContext cc = new CommitContext(session, "not implemented");
        try {
            String exmsg;

            try {
                exmsg = "";
                cc.editor.addSymlink("", "", props, 1);
            } catch (RuntimeException ex) {
                exmsg = ex.getMessage();
            }
            assertEquals("Not implemented: CommitEditor.addSymlink", exmsg);

            try {
                exmsg = "";
                cc.editor.alterSymlink("", 1, "", null);
            } catch (RuntimeException ex) {
                exmsg = ex.getMessage();
            }
            assertEquals("Not implemented: CommitEditor.alterSymlink", exmsg);

            // try {
            //     exmsg = "";
            //     cc.editor.rotate(rotation);
            // } catch (RuntimeException ex) {
            //     exmsg = ex.getMessage();
            // }
            // assertEquals("Not implemented: CommitEditor.rotate", exmsg);
        } finally {
            cc.editor.dispose();
        }
    }

    private static final class LogMsg
    {
        public Set<ChangePath> changedPaths;
        public long revision;
        public Map<String, byte[]> revprops;
        public boolean hasChildren;
    }

    private static final class LogReceiver implements LogMessageCallback
    {
        public final ArrayList<LogMsg> logs = new ArrayList<LogMsg>();

        public void singleMessage(Set<ChangePath> changedPaths,
                                  long revision,
                                  Map<String, byte[]> revprops,
                                  boolean hasChildren)
        {
            LogMsg msg = new LogMsg();
            msg.changedPaths = changedPaths;
            msg.revision = revision;
            msg.revprops = revprops;
            msg.hasChildren = hasChildren;
            logs.add(msg);
        }
    }

    public void testGetLog() throws Exception
    {
        ISVNRemote session = getSession();
        LogReceiver receiver = new LogReceiver();

        session.getLog(null,
                       Revision.SVN_INVALID_REVNUM,
                       Revision.SVN_INVALID_REVNUM,
                       0, false, false, false, null,
                       receiver);

        assertEquals(1, receiver.logs.size());
    }

    public void testGetLogMissing() throws Exception
    {
        ISVNRemote session = getSession();
        LogReceiver receiver = new LogReceiver();

        ArrayList<String> paths = new ArrayList<String>(1);
        paths.add("X");

        boolean exception = false;
        try {
            session.getLog(paths,
                           Revision.SVN_INVALID_REVNUM,
                           Revision.SVN_INVALID_REVNUM,
                           0, false, false, false, null,
                           receiver);
        } catch (ClientException ex) {
            assertEquals("Filesystem has no item",
                         ex.getAllMessages().get(0).getMessage());
            exception = true;
        }

        assertEquals(0, receiver.logs.size());
        assertTrue(exception);
    }

    public void testConfigHandler() throws Exception
    {
        ConfigEvent handler = new ConfigEvent()
            {
                public void onLoad(ISVNConfig cfg)
                {
                    //System.out.println("config:");
                    onecat(cfg.config());
                    //System.out.println("servers:");
                    onecat(cfg.servers());
                }

                private void onecat(ISVNConfig.Category cat)
                {
                    for (String sec : cat.sections()) {
                        //System.out.println("  [" + sec + "]");
                        ISVNConfig.Enumerator en = new ISVNConfig.Enumerator()
                            {
                                public void option(String name, String value)
                                {
                                    //System.out.println("    " + name
                                    //                   + " = " + value);
                                }
                            };
                        cat.enumerate(sec, en);
                    }
                }
            };

        ISVNRemote session = getSession(getTestRepoUrl(),
                                        super.conf.getAbsolutePath(),
                                        handler);
        session.getLatestRevision(); // Make sure the configuration gets loaded
    }

    private static class RemoteStatusReceiver implements RemoteStatus
    {
        static class StatInfo
        {
            public String relpath = null;
            public char kind = ' '; // F, D, L
            public boolean textChanged = false;
            public boolean propsChanged = false;
            public boolean deleted = false;

            StatInfo(String relpath, char kind, boolean added)
            {
                this.relpath = relpath;
                this.kind = kind;
                this.deleted = !added;
            }

            StatInfo(String relpath, char kind,
                     boolean textChanged, boolean propsChanged)
            {
                this.relpath = relpath;
                this.kind = kind;
                this.textChanged = textChanged;
                this.propsChanged = propsChanged;
            }
        }

        public ArrayList<StatInfo> status = new ArrayList<StatInfo>();

        public void addedDirectory(String relativePath)
        {
            status.add(new StatInfo(relativePath, 'D', true));
        }

        public void addedFile(String relativePath)
        {
            status.add(new StatInfo(relativePath, 'F', true));
        }

        public void addedSymlink(String relativePath)
        {
            status.add(new StatInfo(relativePath, 'L', true));
        }

        public void modifiedDirectory(String relativePath,
                                      boolean childrenModified, boolean propsModified)
        {
            status.add(new StatInfo(relativePath, 'D',
                                    childrenModified, propsModified));
        }

        public void modifiedFile(String relativePath,
                                 boolean textModified, boolean propsModified)
        {
            status.add(new StatInfo(relativePath, 'F',
                                    textModified, propsModified));
        }

        public void modifiedSymlink(String relativePath,
                                    boolean targetModified, boolean propsModified)
        {
            status.add(new StatInfo(relativePath, 'L',
                                    targetModified, propsModified));
        }

        public void deleted(String relativePath)
        {
            status.add(new StatInfo(relativePath, ' ', false));
        }
    }

    public void testSimpleStatus() throws Exception
    {
        ISVNRemote session = getSession();

        RemoteStatusReceiver receiver = new RemoteStatusReceiver();
        ISVNReporter rp = session.status(null, Revision.SVN_INVALID_REVNUM,
                                         Depth.infinity, receiver);
        try {
            rp.setPath("", 0, Depth.infinity, true, null);
            assertEquals(1, rp.finishReport());
        } finally {
            rp.dispose();
        }
        assertEquals(21, receiver.status.size());
    }

    public void testPropchangeStatus() throws Exception
    {
        ISVNRemote session = getSession();

        CommitMessageCallback cmcb = new CommitMessageCallback() {
                public String getLogMessage(Set<CommitItem> x) {
                    return "Property change on A/D/gamma";
                }
            };
        client.propertySetRemote(getTestRepoUrl() + "/A/D/gamma",
                                 1L, "foo", "bar".getBytes(), cmcb,
                                 false, null, null);

        RemoteStatusReceiver receiver = new RemoteStatusReceiver();
        ISVNReporter rp = session.status(null, Revision.SVN_INVALID_REVNUM,
                                         Depth.infinity, receiver);
        try {
            rp.setPath("", 1, Depth.infinity, false, null);
            assertEquals(2, rp.finishReport());
        } finally {
            rp.dispose();
        }
        assertEquals(4, receiver.status.size());
        RemoteStatusReceiver.StatInfo mod = receiver.status.get(3);
        assertEquals("A/D/gamma", mod.relpath);
        assertEquals('F', mod.kind);
        assertEquals(false, mod.textChanged);
        assertEquals(true, mod.propsChanged);
    }
}