package com.netcore.pnserver;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.netcore.pnserver.util.Util;

import junit.framework.TestCase;

public class TestLoggerUtil extends TestCase {

	@Test
	public void testSplitQuery() throws UnsupportedEncodingException {
		String queryString = "a=1&b=2&c=3";
		assertEquals(3, Util.splitQuery(queryString).size());
	}

	@Test
	public void testGetTrIDDetails(){
		//trid: $clientid-$msgid-$userid-$automationid-$ts
		String trid = "1-2-1-1-160505123000";
		String tridArr[] = Util.getTrIDDetails(trid);
		assertEquals(tridArr[0], "1");
		assertEquals(tridArr[1], "2");
		assertEquals(tridArr[2], "1");
		assertEquals(tridArr[3], "1");
		assertEquals(tridArr[4], "160505123000");
		assertEquals(5, tridArr.length);
	}
}
