/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.filesexist;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobEntryFilesExistTest {
  private Job job;
  private JobEntryFilesExist entry;

  private String existingFile1;
  private String existingFile2;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    KettleLogStore.init();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    job = new Job( null, new JobMeta() );
    entry = new JobEntryFilesExist();

    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );

    job.setStopped( false );

    File f = File.createTempFile( "existingFile", "ext" );
    f.deleteOnExit();
    existingFile1 = f.getPath();

    f = File.createTempFile( "existingFile", "ext" );
    f.deleteOnExit();
    existingFile2 = f.getPath();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetNrErrorsNewBehaviorFalseResult() throws Exception {
    // this tests fix for PDI-10270
    entry.arguments = new String[] { "nonExistingFile.ext" };

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Entry should fail", res.getResult() );
    assertEquals( "Files not found. Result is false. But... No of errors should be zero", 0, res.getNrErrors() );
  }

  @Test
  public void testSetNrErrorsOldBehaviorFalseResult() throws Exception {
    // this tests backward compatibility settings for PDI-10270
    entry.arguments = new String[] { "nonExistingFile1.ext", "nonExistingFile2.ext" };

    entry.setVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "Y" );

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Entry should fail", res.getResult() );
    assertEquals(
      "Files not found. Result is false. And... Number of errors should be the same as number of not found files",
      entry.arguments.length, res.getNrErrors() );
  }

  @Test
  public void testExecuteWithException() throws Exception {
    entry.arguments = new String[] { null };

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Entry should fail", res.getResult() );
    assertEquals( "File with wrong name was specified. One error should be reported", 1, res.getNrErrors() );
  }

  @Test
  public void testExecuteSuccess() throws Exception {
    entry.arguments = new String[] { existingFile1, existingFile2 };

    Result res = entry.execute( new Result(), 0 );

    assertTrue( "Entry failed", res.getResult() );
  }

  @Test
  public void testExecuteFail() throws Exception {
    entry.arguments =
      new String[] { existingFile1, existingFile2, "nonExistingFile1.ext", "nonExistingFile2.ext" };

    Result res = entry.execute( new Result(), 0 );

    assertFalse( "Entry should fail", res.getResult() );
  }
}
