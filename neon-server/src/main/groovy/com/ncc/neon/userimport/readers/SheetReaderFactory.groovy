package com.ncc.neon.userimport.readers

import com.ncc.neon.userimport.exceptions.UnsupportedFiletypeException

import org.springframework.stereotype.Component

@Component
class SheetReaderFactory {
	SheetReader getSheetReader(String type) {
		switch(type) {
			case "csv":
				return new CSVSheetReader()
			case "xlsx":
				return new ExcelSheetReader()
			default:
				throw new UnsupportedFiletypeException("Import of that type of file is not supported.")
		}
	}
}