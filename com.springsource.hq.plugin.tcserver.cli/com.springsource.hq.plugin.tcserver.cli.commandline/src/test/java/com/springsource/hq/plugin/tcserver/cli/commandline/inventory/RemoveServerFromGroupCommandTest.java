/*
 * Copyright (C) 2009-2015  Pivotal Software, Inc
 *
 * This program is is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.springsource.hq.plugin.tcserver.cli.commandline.inventory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import joptsimple.OptionParser;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.springsource.hq.plugin.tcserver.cli.client.inventory.GroupRepository;
import com.springsource.hq.plugin.tcserver.cli.client.schema.Group;
import com.springsource.hq.plugin.tcserver.cli.client.schema.GroupsResponse;
import com.springsource.hq.plugin.tcserver.cli.client.schema.Resource;
import com.springsource.hq.plugin.tcserver.cli.client.schema.ResponseStatus;
import com.springsource.hq.plugin.tcserver.cli.client.schema.ServiceError;
import com.springsource.hq.plugin.tcserver.cli.commandline.OptionParserFactory;

/**
 * Unit test of {@link RemoveServerFromGroupCommand}
 */
public class RemoveServerFromGroupCommandTest {

    private RemoveServerFromGroupCommand removeServerFromGroupCommand;

    private StringWriter errorStringWriter;

    private PrintWriter errorWriter;

    private GroupRepository groupRepository;

    private OptionParser optionParser;

    private OptionParser testOptionParser;

    private StringWriter outputStringWriter;

    private PrintWriter outWriter;

    /**
     * Sets up the tests
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.groupRepository = EasyMock.createMock(GroupRepository.class);
        this.outputStringWriter = new StringWriter();
        this.errorStringWriter = new StringWriter();
        this.errorWriter = new PrintWriter(errorStringWriter);
        this.outWriter = new PrintWriter(outputStringWriter);
        this.optionParser = (OptionParser) new OptionParserFactory().getObject();
        this.removeServerFromGroupCommand = new RemoveServerFromGroupCommand(groupRepository, optionParser, outWriter, errorWriter);
        this.removeServerFromGroupCommand.afterPropertiesSet();
    }

    /**
     * Tears down the tests
     */
    @After
    public void tearDown() {
        this.errorWriter.close();
        this.outWriter.close();
    }

    /**
     * Verifies the removeServer command removes a server from a group
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveServerFromGroupUsingGroupName() throws IOException {
        GroupsResponse response = new GroupsResponse();
        response.setStatus(ResponseStatus.SUCCESS);
        final Group group = new Group();
        group.setName("Group1");
        final Resource server = new Resource();
        server.setId(1234);
        EasyMock.expect(groupRepository.removeServerFromGroup(group, server)).andReturn(response);
        EasyMock.replay(groupRepository);
        int exitCode = removeServerFromGroupCommand.execute(new String[] { "--groupname=Group1", "--serverid=1234" });
        EasyMock.verify(groupRepository);
        final String output = outputStringWriter.getBuffer().toString().trim();
        assertEquals("Command remove-server-from-group executed successfully", output);
        assertEquals(0, exitCode);
    }

    /**
     * Verifies the removeServer command removes a server from a group
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveServerFromGroupUsingGroupId() throws IOException {
        GroupsResponse response = new GroupsResponse();
        response.setStatus(ResponseStatus.SUCCESS);
        final Group group = new Group();
        group.setId(123);
        final Resource server = new Resource();
        server.setId(1234);
        EasyMock.expect(groupRepository.removeServerFromGroup(group, server)).andReturn(response);
        EasyMock.replay(groupRepository);
        int exitCode = removeServerFromGroupCommand.execute(new String[] { "--groupid=123", "--serverid=1234" });
        EasyMock.verify(groupRepository);
        final String output = outputStringWriter.getBuffer().toString().trim();
        assertEquals("Command remove-server-from-group executed successfully", output);
        assertEquals(0, exitCode);
    }

    /**
     * Verifies that a failure to execute command is reported correctly
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveServerFailure() throws IOException {
        final Group group = new Group();
        group.setName("Group1");
        GroupsResponse response = new GroupsResponse();
        response.setStatus(ResponseStatus.FAILURE);
        ServiceError error = new ServiceError();
        error.setReasonText("Something went wrong");
        response.setError(error);
        final Resource server = new Resource();
        server.setName("server1");
        EasyMock.expect(groupRepository.removeServerFromGroup(group, server)).andReturn(response);
        EasyMock.replay(groupRepository);
        int exitCode = removeServerFromGroupCommand.execute(new String[] { "--groupname=Group1", "--servername=server1" });
        EasyMock.verify(groupRepository);
        final String output = errorStringWriter.getBuffer().toString().trim();
        assertEquals("Failed to execute command remove-server-from-group.  Reason: Something went wrong", output);
        assertEquals(1, exitCode);
    }

    /**
     * Verifies the help command output is correct
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveServerHelp() throws IOException {
        final int exitCode = removeServerFromGroupCommand.execute(new String[] { "--help" });
        final String output = outputStringWriter.getBuffer().toString().trim();
        assertEquals(getHelpOutput(), output);
        assertEquals(0, exitCode);
    }

    /**
     * Verifies an error is printed when groupname argument is missing
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveServerMissingNameOrGroupId() throws IOException {
        final int exitCode = removeServerFromGroupCommand.execute(new String[0]);
        final String output = errorStringWriter.getBuffer().toString().trim();
        assertEquals(1, exitCode);
        assertEquals("Either the name or ID of a group must be specified", output);
    }

    @Test
    public void testRemoveServerWithServerNameAndServerId() throws IOException {
        final int exitCode = removeServerFromGroupCommand.execute(new String[] { "--servername=test-server", "--serverid=123",
            "--groupname=test-group" });
        final String output = errorStringWriter.getBuffer().toString().trim();
        assertEquals("Only serverid or servername can be specified, not both.", output);
        assertEquals(1, exitCode);
    }

    /**
     * Verifies an error is printed when serverid and servername are missing
     * 
     * @throws IOException
     */
    @Test
    public void testRemoveServerMissingServerIdOrName() throws IOException {
        final int exitCode = removeServerFromGroupCommand.execute(new String[] { "--groupname=Group1" });
        final String output = errorStringWriter.getBuffer().toString().trim();
        assertEquals(1, exitCode);
        assertEquals("Either the name or ID of a server must be specified", output);
    }

    private String getHelpOutput() {
        String output = null;

        try {
            testOptionParser = (OptionParser) new OptionParserFactory().getObject();
            testOptionParser.accepts(RemoveServerFromGroupCommand.OPT_SERVER_NAME, RemoveServerFromGroupCommand.OPT_SERVER_NAME_DESC).withRequiredArg().ofType(
                String.class);
            testOptionParser.accepts(RemoveServerFromGroupCommand.OPT_SERVER_ID, RemoveServerFromGroupCommand.OPT_SERVER_ID_DESC).withRequiredArg().ofType(
                Integer.class);
            testOptionParser.accepts(RemoveServerFromGroupCommand.OPT_GROUP_NAME, RemoveServerFromGroupCommand.OPT_GROUP_NAME_DESC).withRequiredArg().ofType(
                String.class);
            testOptionParser.accepts(RemoveServerFromGroupCommand.OPT_GROUP_ID, RemoveServerFromGroupCommand.OPT_GROUP_ID_DESC).withRequiredArg().ofType(
                Integer.class);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(removeServerFromGroupCommand.getName() + ": " + removeServerFromGroupCommand.getDescription());

            testOptionParser.printHelpOn(pw);

            output = sw.getBuffer().toString().trim();

        } catch (IOException e) {
            // Errors with the testOptionParser.printHelpOn() call
            e.printStackTrace();
        } catch (Exception e) {
            // Errors with the creation of the testOptionParser
            e.printStackTrace();
        }

        return output;
    }

}
