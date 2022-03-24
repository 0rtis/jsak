package io.ortis.jsak.log.config;

import io.ortis.jsak.log.LogService;

import java.util.List;
import java.util.logging.Level;

public interface LogServiceConfig
{
	Level getLevel();

	List<LogService.Listener> getOutputs();
}
