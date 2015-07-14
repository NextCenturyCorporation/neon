package com.ncc.neon.user_import

import org.apache.commons.io.LineIterator

public interface ImportHelper {
	public List uploadData(String host, String identifier, LineIterator reader)

	public boolean dropData(String host, String identifier)

	public List convertFields(String host, String identifier, UserFieldDataBundle bundle)
}