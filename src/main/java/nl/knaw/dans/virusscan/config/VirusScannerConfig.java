/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.virusscan.config;

import nl.knaw.dans.lib.util.ExecutorServiceFactory;
import nl.knaw.dans.virusscan.config.validation.ValidClamdBufferSize;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.regex.Pattern;

public class VirusScannerConfig {

    @NotNull
    private Pattern resultPositivePattern;
    @NotNull
    @Valid
    @javax.validation.constraints.Pattern(regexp = "[^%]*%1[^%]*", message = "resultPositiveMessageTemplate should have exactly 1 substitution parameter")
    private String resultPositiveMessageTemplate;
    @NotNull
    private ExecutorServiceFactory scanDatasetTaskQueue;
    @NotNull
    private ExecutorServiceFactory resumeDatasetTaskQueue;
    @Valid
    @NotNull
    private Pattern resultNegativePattern;
    @Valid
    @NotNull
    @ValidClamdBufferSize
    private ClamdConfig clamd;
    @Valid
    @NotNull
    private ResumeTasksConfig resumeTasks;

    public ResumeTasksConfig getResumeTasks() {
        return resumeTasks;
    }

    public void setResumeTasks(ResumeTasksConfig resumeTasks) {
        this.resumeTasks = resumeTasks;
    }

    public ClamdConfig getClamd() {
        return clamd;
    }

    public void setClamd(ClamdConfig clamd) {
        this.clamd = clamd;
    }

    public ExecutorServiceFactory getScanDatasetTaskQueue() {
        return scanDatasetTaskQueue;
    }

    public void setScanDatasetTaskQueue(ExecutorServiceFactory scanDatasetTaskQueue) {
        this.scanDatasetTaskQueue = scanDatasetTaskQueue;
    }

    public ExecutorServiceFactory getResumeDatasetTaskQueue() {
        return resumeDatasetTaskQueue;
    }

    public void setResumeDatasetTaskQueue(ExecutorServiceFactory resumeDatasetTaskQueue) {
        this.resumeDatasetTaskQueue = resumeDatasetTaskQueue;
    }

    public Pattern getResultPositivePattern() {
        return resultPositivePattern;
    }

    public void setResultPositivePattern(Pattern resultPositivePattern) {
        this.resultPositivePattern = resultPositivePattern;
    }

    public Pattern getResultNegativePattern() {
        return resultNegativePattern;
    }

    public void setResultNegativePattern(Pattern resultNegativePattern) {
        this.resultNegativePattern = resultNegativePattern;
    }

    public String getResultPositiveMessageTemplate() {
        return resultPositiveMessageTemplate;
    }

    public void setResultPositiveMessageTemplate(String resultPositiveMessageTemplate) {
        this.resultPositiveMessageTemplate = resultPositiveMessageTemplate;
    }

}
/*
  # Location of the clamd daemon clamd:
      host:localhost port:3310
      # chunk size in bytes chunksize:1048576
      # Pattern to match a positive.The must be one group which will be used in the resume message resultPositivePattern:'^stream: (.*)$'
      # If clamscan's response matches this pattern, the file is declared OK resultNegativePattern:'^stream:OK$'

      #
      # The message that will be sent back to Dataverse when one or more virus are found.The%1will be replaced with a comma-separated list of(file,message)pairs,e.g.,
      #
      # 'Virus found in dataset: subdir/eicar.com.txt -> Win.Test.EICAR_HDB-1 FOUND, eicarcom2.zip -> Win.Test.EICAR_HDB-1 FOUND
      #
      # The message will be taken from matches of resultPostivePattern
      #
      resultPostiveMessageTemplate:'Virus found in dataset: %1'

      #
      # Configures the worker threads that will scan datasets for virus
      #
      scanDatasetTaskQueue:
      nameFormat:"scan-dataset-worker-%d"

      maxQueueSize:4
      # Number of threads will be increased when maxQueueSize is exceeded.
      minThreads:2
      # No more than maxThreads will be created though maxThreads:10
      # Threads will die after 60seconds of idleness keepAliveTime:60seconds

      resumeTasks:
      maxTries:10
      waitBetweenTries:3seconds

      #
      # Configures the worker threads that will resume the workflow.
      #
      resumeDatasetTaskQueue:
      nameFormat:"resume-dataset-worker-%d"

      maxQueueSize:4
      # Number of threads will be increased when maxQueueSize is exceeded.
      minThreads:2
      # No more than maxThreads will be created though maxThreads:10
      # Threads will die after 60seconds of idleness keepAliveTime:60seconds


*/