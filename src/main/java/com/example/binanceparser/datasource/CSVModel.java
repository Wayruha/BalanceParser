package com.example.binanceparser.datasource;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "event_type", "event_ts", "json" })
public class CSVModel {
	private String event_type;
	private String event_ts;
	private String json;
}
