
package org.drip.xva.derivative;

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
 * EdgeReplicationPortfolio contains the Dynamic Replicating Portfolio of the Pay-out using the Assets in the
 * 	Economy, from the Bank's View Point. The References are:
 *  
 *  - Burgard, C., and M. Kjaer (2014): PDE Representations of Derivatives with Bilateral Counter-party Risk
 *  	and Funding Costs, Journal of Credit Risk, 7 (3) 1-19.
 *  
 *  - Cesari, G., J. Aquilina, N. Charpillon, X. Filipovic, G. Lee, and L. Manda (2009): Modeling, Pricing,
 *  	and Hedging Counter-party Credit Exposure - A Technical Guide, Springer Finance, New York.
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

public class EdgeReplicationPortfolio {
	private double _dblAssetUnits = java.lang.Double.NaN;
	private double _dblCashAccount = java.lang.Double.NaN;
	private double _dblBankBondUnits = java.lang.Double.NaN;
	private double _dblCounterPartyBondUnits = java.lang.Double.NaN;

	/**
	 * EdgeReplicationPortfolio Constructor
	 * 
	 * @param dblAssetUnits The Number of Asset Replication Units
	 * @param dblBankBondUnits The Number of Bank Zero Coupon Bond Replication Units
	 * @param dblCounterPartyBondUnits The Number of Counter Party Zero Coupon Bond Replication Units
	 * @param dblCashAccount The Cash Account
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public EdgeReplicationPortfolio (
		final double dblAssetUnits,
		final double dblBankBondUnits,
		final double dblCounterPartyBondUnits,
		final double dblCashAccount)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblAssetUnits = dblAssetUnits) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblBankBondUnits = dblBankBondUnits) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblCounterPartyBondUnits =
					dblCounterPartyBondUnits) || dblCounterPartyBondUnits > 0. ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblCashAccount = dblCashAccount))
			throw new java.lang.Exception ("EdgeReplicationPortfolio Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Number of Asset Replication Units
	 * 
	 * @return The Number of Asset Replication Units
	 */

	public double assetUnits()
	{
		return _dblAssetUnits;
	}

	/**
	 * Retrieve the Number of Bank Zero Coupon Bond Replication Units
	 * 
	 * @return The Number of Bank Zero Coupon Bond Replication Units
	 */

	public double bankBondUnits()
	{
		return _dblBankBondUnits;
	}

	/**
	 * Retrieve the Number of Counter Party Zero Coupon Bond Replication Units
	 * 
	 * @return The Number of Counter Party Zero Coupon Bond Replication Units
	 */

	public double counterPartyBondUnits()
	{
		return _dblCounterPartyBondUnits;
	}

	/**
	 * Retrieve the Cash Account Amount
	 * 
	 * @return The Cash Account Amount
	 */

	public double cashAccount()
	{
		return _dblCashAccount;
	}

	/**
	 * Compute the Market Value of the Portfolio
	 * 
	 * @param us The Trade-able Asset Market Snapshot
	 * 
	 * @return The Market Value of the Portfolio
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double value (
		final org.drip.xva.definition.UniverseSnapshot us)
		throws java.lang.Exception
	{
		if (null == us) throw new java.lang.Exception ("EdgeReplicationPortfolio::value => Invalid Inputs");

		return -1. * (_dblAssetUnits * us.assetNumeraire().finish() + _dblBankBondUnits *
			us.zeroCouponBankBondNumeraire().finish() + _dblCounterPartyBondUnits *
				us.zeroCouponCounterPartyBondNumeraire().finish() + _dblCashAccount);
	}
}
