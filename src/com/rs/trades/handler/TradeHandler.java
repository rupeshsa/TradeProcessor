package com.rs.trades.handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.trades.Trade;

public class TradeHandler {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private List<Trade> tradeList = new ArrayList<Trade>();
	private ConcurrentHashMap<String, Trade> tradeMap = new ConcurrentHashMap<String, Trade>();

	public static void main(String[] args) throws Exception {
		TradeHandler handler = new TradeHandler();
		handler.init();
		handler.handleTrade();
	}

	private void init() throws Exception {
		tradeList.add(new Trade("T1", 1, "CP-1", "B1", sdf.parse("20/05/2023"), new Date(), "N"));
		tradeList.add(new Trade("T2", 2, "CP-2", "B1", sdf.parse("20/05/2023"), new Date(), "N"));
		tradeList.add(new Trade("T2", 1, "CP-1", "B1", sdf.parse("20/05/2023"), sdf.parse("14/03/2015"), "N"));
		tradeList.add(new Trade("T3", 3, "CP-3", "B2", sdf.parse("20/05/2014"), new Date(), "N"));
	}

	public void handleTrade() {
		Date todaysDate = new Date();
		tradeList.forEach(trade -> {
			try {
				if (tradeMap.containsKey(trade.getTradeId())) {
					validateVersionSequence(trade);
				} else {
					validateMaturityDate(todaysDate, trade);
				}

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		});

		tradeMap.entrySet().forEach(entry -> {
			System.out.println(entry.getKey() + " " + entry.getValue());
		});
	}

	/**
	 * Method to validate Maturity Date
	 * 
	 * @param todaysDate
	 * @param trade
	 */
	public ConcurrentHashMap<String, Trade> validateMaturityDate(Date todaysDate, Trade trade) {
		// 2.Store should not allow the trade which has less maturity date then today
		// date.
		if (trade.getMaturityDate().after(todaysDate)) {
			tradeMap.put(trade.getTradeId(), trade);
		} else {
			// 3.Store should automatically update the expire flag if in a store the trade
			// crosses the maturity date.
			trade.setExpired("Y");
			tradeMap.put(trade.getTradeId(), trade);
			System.out.println("Expired flag is updated as Trade is expired, Trade ID: "+trade.getTradeId());
		}
		return tradeMap;
	}

	/**
	 * Method to validate Version Sequence
	 * 
	 * @param trade
	 * @throws Exception
	 */
	public ConcurrentHashMap<String, Trade> validateVersionSequence(Trade trade) throws Exception {
		/*
		 * 1.if the lower version is being received by the store it will reject the
		 * trade and throw an exception. If the version is same it will override the
		 * existing record.
		 */
		Trade prevTrade = tradeMap.get(trade.getTradeId());
		if (prevTrade==null || ((trade.getVersion() > prevTrade.getVersion())
				|| (trade.getVersion().intValue() == prevTrade.getVersion().intValue()))) {
			tradeMap.put(trade.getTradeId(), trade);
		} else {
			throw new Exception("Lower version encountered..Trade rejected with trade id-" + trade.getTradeId());
		}
		return tradeMap;
	}

}
