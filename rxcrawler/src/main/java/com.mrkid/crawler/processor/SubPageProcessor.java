package com.mrkid.crawler.processor;

import com.mrkid.crawler.Page;
import com.mrkid.crawler.RequestMatcher;

public interface SubPageProcessor extends RequestMatcher {

	/**
	 * process the page, extract urls to fetch, extract the data and store
	 *
	 * @param page page
	 *
	 * @return whether continue to match
	 */
	MatchOther processPage(Page page) throws Exception;

}
