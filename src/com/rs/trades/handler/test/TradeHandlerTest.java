package com.rs.trades.handler.test;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;

import com.rs.trades.Trade;
import com.rs.trades.handler.TradeHandler;

public class TradeHandlerTest {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	TradeHandler handler = new TradeHandler();
	
	@Test
	public void validateHappyPathTest() throws Exception {
		Trade trade= new Trade("T1", 1, "CP-1", "B1", sdf.parse("20/05/2020"), new Date(), "N");
		ConcurrentHashMap<String, Trade> tradeMap = handler.validateVersionSequence(trade);
		assertEquals(1,tradeMap.size());
	}
	
	
	@SuppressWarnings("unused")
	@Test(expected = Exception.class) 
	public void validateLowerVersionTest() throws Exception {
		Trade trade= new Trade("T2", 2, "CP-2", "B1", sdf.parse("20/05/2021"), new Date(), "N");
		ConcurrentHashMap<String, Trade> tradeMap = handler.validateVersionSequence(trade);
		
		trade= new Trade("T2", 1, "CP-1", "B1", sdf.parse("20/05/2021"), sdf.parse("14/03/2015"), "N");
		tradeMap = handler.validateVersionSequence(trade);
		
		Assert.fail("Lower version encountered..Trade rejected with trade id-T2");
	}
	
	@Test
	public void validateMaturityDateTest() throws Exception {
		Trade trade= new Trade("T2", 1, "CP-1", "B1", sdf.parse("20/05/2021"), sdf.parse("14/03/2015"), "N");
		ConcurrentHashMap<String, Trade> tradeMap = handler.validateMaturityDate(new Date(), trade);
		
		assertEquals(1,tradeMap.size());
		assertEquals("Y",tradeMap.get(trade.getTradeId()).isExpired());
	}
}
