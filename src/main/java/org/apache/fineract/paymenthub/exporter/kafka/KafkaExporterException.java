/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package org.apache.fineract.paymenthub.exporter.kafka;

public class KafkaExporterException extends RuntimeException {

  public KafkaExporterException(final String message) {
    super(message);
  }

  public KafkaExporterException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
