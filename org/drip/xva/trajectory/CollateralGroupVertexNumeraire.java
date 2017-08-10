
package org.drip.xva.trajectory;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2017 Lakshmi Krishnamurthy
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
 * CollateralGroupVertexNumeraire holds the Vertex Market Numeraire Realizations of a Projected Path of a
 *  Simulation Run along the Granularity of a Collateral Group. The References are:
 *  
 *  - Burgard, C., and M. Kjaer (2014): PDE Representations of Derivatives with Bilateral Counter-party Risk
 *  	and Funding Costs, Journal of Credit Risk, 7 (3) 1-19.
 *  
 *  - Burgard, C., and M. Kjaer (2014): In the Balance, Risk, 24 (11) 72-75.
 *  
 *  - Gregory, J. (2009): Being Two-faced over Counter-party Credit Risk, Risk 20 (2) 86-90.
 *  
 *  - Li, B., and Y. Tang (2007): Quantitative Analysis, Derivatives Modeling, and Trading Strategies in the
 *  	Presence of Counter-party Credit Risk for the Fixed Income Market, World Scientific Publishing,
 *  	Singapore.
 * 
 *  - Piterbarg, V. (2010): Funding Beyond Discounting: Collateral Agreements and Derivatives Pricing, Risk
 *  	21 (2) 97-102.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CollateralGroupVertexNumeraire {
	private double _dblCSA = java.lang.Double.NaN;
	private double _dblBankRecovery = java.lang.Double.NaN;
	private double _dblBankSurvival = java.lang.Double.NaN;
	private double _dblBankFundingSpread = java.lang.Double.NaN;
	private double _dblCounterPartyRecovery = java.lang.Double.NaN;
	private double _dblCounterPartySurvival = java.lang.Double.NaN;

	/**
	 * CollateralGroupVertexNumeraire Constructor
	 * 
	 * @param dblCSA The Realized CSA Numeraire
	 * @param dblBankSurvival The Realized Bank Survival Numeraire
	 * @param dblBankRecovery The Realized Bank Recovery Numeraire
	 * @param dblBankFundingSpread The Bank Funding Spread Numeraire
	 * @param dblCounterPartySurvival The Realized Counter Party Survival Numeraire
	 * @param dblCounterPartyRecovery The Realized Counter Party Recovery Numeraire
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralGroupVertexNumeraire (
		final double dblCSA,
		final double dblBankSurvival,
		final double dblBankRecovery,
		final double dblBankFundingSpread,
		final double dblCounterPartySurvival,
		final double dblCounterPartyRecovery)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblCSA = dblCSA) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblBankSurvival = dblBankSurvival) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblBankRecovery = dblBankRecovery) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblBankFundingSpread = dblBankFundingSpread)
						|| !org.drip.quant.common.NumberUtil.IsValid (_dblCounterPartySurvival =
							dblCounterPartySurvival) || !org.drip.quant.common.NumberUtil.IsValid
								(_dblCounterPartyRecovery = dblCounterPartyRecovery))
			throw new java.lang.Exception ("CollateralGroupVertexNumeraire Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Realized CSA Numeraire
	 * 
	 * @return The Realized CSA Numeraire
	 */

	public double csa()
	{
		return _dblCSA;
	}

	/**
	 * Retrieve the Realized Bank Survival Numeraire
	 * 
	 * @return The Realized Bank Survival Numeraire
	 */

	public double bankSurvival()
	{
		return _dblBankSurvival;
	}

	/**
	 * Retrieve the Realized Bank Recovery Numeraire
	 * 
	 * @return The Realized Bank Recovery Numeraire
	 */

	public double bankRecovery()
	{
		return _dblBankRecovery;
	}

	/**
	 * Retrieve the Realized Bank Funding Spread
	 * 
	 * @return The Realized Bank Funding Spread
	 */

	public double bankFundingSpread()
	{
		return _dblBankFundingSpread;
	}

	/**
	 * Retrieve the Realized Counter Party Survival Numeraire
	 * 
	 * @return The Realized Counter Party Survival Numeraire
	 */

	public double counterPartySurvival()
	{
		return _dblCounterPartySurvival;
	}

	/**
	 * Retrieve the Realized Counter Party Recovery Numeraire
	 * 
	 * @return The Realized Counter Party Recovery Numeraire
	 */

	public double counterPartyRecovery()
	{
		return _dblCounterPartyRecovery;
	}
}
