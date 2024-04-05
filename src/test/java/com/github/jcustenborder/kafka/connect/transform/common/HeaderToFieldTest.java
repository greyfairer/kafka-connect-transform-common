package com.github.jcustenborder.kafka.connect.transform.common;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.jcustenborder.kafka.connect.utils.AssertStruct.assertStruct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HeaderToFieldTest {
  Transformation<SinkRecord> transformation;

  @Test
  public void apply() throws IOException {
    this.transformation = new HeaderToField.Value<>();

    this.transformation.configure(
        ImmutableMap.of(HeaderToFieldConfig.HEADER_MAPPINGS_CONF, "applicationId:STRING")
    );

    ConnectHeaders inputHeaders = new ConnectHeaders();
    inputHeaders.addString("applicationId", "testing");

    Schema inputSchema = SchemaBuilder.struct()
        .field("firstName", Schema.OPTIONAL_STRING_SCHEMA)
        .field("lastName", Schema.OPTIONAL_STRING_SCHEMA)
        .build();

    Struct inputStruct = new Struct(inputSchema)
        .put("firstName", "example")
        .put("lastName", "user");

    Schema expectedSchema = SchemaBuilder.struct()
        .field("firstName", Schema.OPTIONAL_STRING_SCHEMA)
        .field("lastName", Schema.OPTIONAL_STRING_SCHEMA)
        .field("applicationId", Schema.OPTIONAL_STRING_SCHEMA)
        .build();
    Struct expectedStruct = new Struct(expectedSchema)
        .put("firstName", "example")
        .put("lastName", "user")
        .put("applicationId", "testing");

    SinkRecord inputRecord = new SinkRecord(
        "testing",
        1,
        null,
        null,
        inputStruct.schema(),
        inputStruct,
        12345L,
        123412351L,
        TimestampType.NO_TIMESTAMP_TYPE,
        inputHeaders
    );

    SinkRecord actualRecord = this.transformation.apply(inputRecord);
    assertNotNull(actualRecord, "record should not be null.");
    assertStruct(expectedStruct, (Struct) actualRecord.value());
  }

  @Test
  public void applyWithMap() throws IOException {
    this.transformation = new HeaderToField.Value<>();

    this.transformation.configure(
            ImmutableMap.of(HeaderToFieldConfig.HEADER_MAPPINGS_CONF, "applicationId:STRING")
    );

    ConnectHeaders inputHeaders = new ConnectHeaders();
    inputHeaders.addString("applicationId", "testing");

    Map<String, Object> value = new HashMap<>();
    value.put("firstName", "example");
    value.put("lastName", "user");


    SinkRecord inputRecord = new SinkRecord(
            "testing",
            1,
            null,
            null,
            null,
            value,
            12345L,
            123412351L,
            TimestampType.NO_TIMESTAMP_TYPE,
            inputHeaders
    );

    SinkRecord actualRecord = this.transformation.apply(inputRecord);
    assertNotNull(actualRecord, "record should not be null.");
    assertNull(actualRecord.valueSchema(), "record's valueSchema should be null.");
    assertEquals("testing", ((Map<String, String>)actualRecord.value()).get("applicationId"));
  }

}
