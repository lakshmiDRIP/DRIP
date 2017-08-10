
package org.drip.param.valuation;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2017 Lakshmi Krishnamurthy
 * Copyright (C) 2016 Lakshmi Krishnamurthy
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for buy/side financial/trading model
 *  	libraries targeting analysts and developers
 *  	https://lakshmidrip.github.io/DRIP/
 *  
 *  DRIP is composed of four main libraries:
 *  
 *  - DRIP Fixed Income - https://lakshmidrip.github.io/DRIP-Fixed-Income/
 *  - DRIP Asset Allocation - https://lakshmidrip.github.io/DRIP-Asset-Allocation/
 *  - DRIP Numerical Optimizer - https://lakshmidrip.github.io/DRIP-Numerical-Optimizer/
 *  - DRIP Statistical Learning - https://lakshmidrip.github.io/DRIP-Statistical-Learning/
 * 
 *  - DRIP Fixed Income: Library for Instrument/Trading Conventions, Treasury Futures/Options,
 *  	Funding/Forward/Overnight Curves, Multi-Curve Construction/Valuation, Collateral Valuation and XVA
 *  	Metric Generation, Calibration and Hedge Attributions, Statistical Curve Construction, Bond RV
 *  	Metrics, Stochastic Evolution and Option Pricing, Interest Rate Dynamics and Option Pricing, LMM
 *  	Extensions/Calibrations/Greeks, Algorithmic Differentiation, and Asset Backed Models and Analytics.
 * 
 *  - DRIP Asset Allocation: Library for model libraries for MPT framework, Black Litterman Strategy
 *  	Incorporator, Holdings Constraint, and Transaction Costs.
 * 
 *  - DRIP Numerical Optimizer: Library for Numerical Optimization and Spline Functionality.
 * 
 *  - DRIP Statistical Learning: Library for Statistical Evaluation and Machine Learning.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * ValuationCustomizationParams holds the parameters needed to interpret the input quotes. It contains the
 * 	quote day count, the quote frequency, the quote EOM Adjustment, the quote Act/Act parameters, the quote
 * 	Calendar, the Core Collateralization Parameters, and the Switchable Alternate Collateralization
 * 	Parameters. It also indicates if the native quote is spread based.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ValuationCustomizationParams {
	private int _iYieldFrequency = 0;
	private boolean _bSpreadQuoted = false;
	private java.lang.String _strYieldDC = "";
	private boolean _bYieldApplyEOMAdj = false;
	private java.lang.String _strYieldCalendar = "";
	private org.drip.analytics.daycount.ActActDCParams _aapYield = null;

	/**
	 * Construct ValuationCustomizationParams from the Day Count and the Frequency parameters
	 * 
	 * @param strDC Quoting Day Count
	 * @param iFrequency Quoting Frequency
	 * @param bApplyEOMAdj TRUE - Apply the EOM Adjustment
	 * @param aap - Quoting Act/Act Parameters
	 * @param strCalendar - Quoting Calendar
	 * @param bSpreadQuoted - TRUE - Market Quotes are Spread Quoted
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ValuationCustomizationParams (
		final java.lang.String strDC,
		final int iFrequency,
		final boolean bApplyEOMAdj,
		final org.drip.analytics.daycount.ActActDCParams aap,
		final java.lang.String strCalendar,
		final boolean bSpreadQuoted)
		throws java.lang.Exception
	{
		if (null == strDC || strDC.isEmpty() || 0 == iFrequency)
			throw new java.lang.Exception ("ValuationCustomizationParams ctr: Invalid quoting params!");

		_aapYield = aap;
		_strYieldDC = strDC;
		_iYieldFrequency = iFrequency;
		_bSpreadQuoted = bSpreadQuoted;
		_strYieldCalendar = strCalendar;
		_bYieldApplyEOMAdj = bApplyEOMAdj;
	}

	/**
	 * Retrieve the Yield Act Act Day Count Parameters
	 * 
	 * @return The Yield Act Act Day Count Parameters
	 */

	public org.drip.analytics.daycount.ActActDCParams yieldAAP()
	{
		return _aapYield;
	}

	/**
	 * Retrieve the Yield Day Count
	 * 
	 * @return The Yield Day Count
	 */

	public java.lang.String yieldDayCount()
	{
		return _strYieldDC;
	}

	/**
	 * Retrieve the Yield Frequency
	 * 
	 * @return The Yield Frequency
	 */

	public int yieldFreq()
	{
		return _iYieldFrequency;
	}

	/**
	 * Indicate if spread Quoted
	 * 
	 * @return TRUE - Spread Quoted
	 */

	public boolean spreadQuoted()
	{
		return _bSpreadQuoted;
	}

	/**
	 * Retrieve the Yield Calendar
	 * 
	 * @return The Yield Calendar
	 */

	public java.lang.String yieldCalendar()
	{
		return _strYieldCalendar;
	}

	/**
	 * Indicate if EOM Adjustment is to be made for the Yield Calculation
	 * 
	 * @return TRUE - EOM Adjustment is to be made for the Yield Calculation
	 */

	public boolean applyYieldEOMAdj()
	{
		return _bYieldApplyEOMAdj;
	}
}
