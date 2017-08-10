
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
 * CollateralGroupEdge holds the Edge that contains the Vertex Realizations of a Projected Path of a Single
 *  Simulation Run along the Granularity of a Netting Group. The References are:
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

public class CollateralGroupEdge {
	private org.drip.xva.trajectory.CollateralGroupVertex _cgvHead = null;
	private org.drip.xva.trajectory.CollateralGroupVertex _cgvTail = null;

	/**
	 * CollateralGroupEdge Constructor
	 * 
	 * @param cgvHead The Head Vertex
	 * @param cgvTail The Tail Vertex
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CollateralGroupEdge (
		final org.drip.xva.trajectory.CollateralGroupVertex cgvHead,
		final org.drip.xva.trajectory.CollateralGroupVertex cgvTail)
		throws java.lang.Exception
	{
		if (null == (_cgvHead = cgvHead) || null == (_cgvTail = cgvTail) || _cgvHead.vertex().julian() >=
			_cgvTail.vertex().julian())
			throw new java.lang.Exception ("CollateralGroupEdge Constructor => Invalid Inputs");
	}

	/**
	 * Retrieve the Head Vertex
	 * 
	 * @return The Head Vertex
	 */

	public org.drip.xva.trajectory.CollateralGroupVertex head()
	{
		return _cgvHead;
	}

	/**
	 * Retrieve the Tail Vertex
	 * 
	 * @return The Tail Vertex
	 */

	public org.drip.xva.trajectory.CollateralGroupVertex tail()
	{
		return _cgvTail;
	}

	/**
	 * Compute the Period Edge Credit Adjustment
	 * 
	 * @return The Period Edge Credit Adjustment
	 */

	public double credit()
	{
		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnHead = _cgvHead.numeraire();

		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnTail = _cgvTail.numeraire();

		return 0.5 * (_cgvTail.exposure().collateralizedPositive() / cgvnTail.csa() +
			_cgvHead.exposure().collateralizedPositive() / cgvnHead.csa()) * (cgvnTail.counterPartySurvival()
				- cgvnHead.counterPartySurvival()) * (1. - 0.5 * (cgvnHead.counterPartyRecovery() +
					cgvnTail.counterPartyRecovery()));
	}

	/**
	 * Compute the Period Edge Debt Adjustment
	 * 
	 * @return The Period Edge Debt Adjustment
	 */

	public double debt()
	{
		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnHead = _cgvHead.numeraire();

		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnTail = _cgvTail.numeraire();

		return 0.5 * (_cgvTail.exposure().collateralizedNegative() / cgvnTail.csa() +
			_cgvHead.exposure().collateralizedNegative() / cgvnHead.csa()) * (cgvnTail.bankSurvival() -
				cgvnHead.bankSurvival()) * (1. - 0.5 * (cgvnHead.bankRecovery() + cgvnTail.bankRecovery()));
	}

	/**
	 * Compute the Period Edge Funding Adjustment
	 * 
	 * @return The Period Edge Funding Adjustment
	 */

	public double funding()
	{
		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnHead = _cgvHead.numeraire();

		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnTail = _cgvTail.numeraire();

		return -0.5 * (_cgvTail.exposure().collateralizedPositive() / cgvnTail.csa() *
			cgvnTail.bankFundingSpread() + _cgvHead.exposure().collateralizedPositive() / cgvnHead.csa() *
				cgvnHead.bankFundingSpread()) * (_cgvTail.vertex().julian() - _cgvHead.vertex().julian()) /
					365.25;
	}

	/**
	 * Compute the Period Edge Total Adjustment
	 * 
	 * @return The Period Edge Total Adjustment
	 */

	public double total()
	{
		return credit() + debt() + funding();
	}

	/**
	 * Generate the Assorted XVA Adjustment Metrics
	 * 
	 * @return The Assorted XVA Adjustment Metrics
	 */

	public org.drip.xva.trajectory.CollateralGroupEdgeAdjustment generate()
	{
		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnHead = _cgvHead.numeraire();

		org.drip.xva.trajectory.CollateralGroupVertexNumeraire cgvnTail = _cgvTail.numeraire();

		double dblEdgeBankRecovery = 0.5 * (cgvnHead.bankRecovery() + cgvnTail.bankRecovery());

		double dblEdgeCounterPartyRecovery = 0.5 * (cgvnHead.counterPartyRecovery() +
			cgvnTail.counterPartyRecovery());

		org.drip.xva.trajectory.CollateralGroupVertexExposure cgveHead = _cgvHead.exposure();

		org.drip.xva.trajectory.CollateralGroupVertexExposure cgveTail = _cgvTail.exposure();

		double dblHeadCollateralizedPositiveExposure = cgveHead.collateralizedPositive();

		double dblTailCollateralizedPositiveExposure = cgveTail.collateralizedPositive();

		double dblHeadCollateralizedNegativeExposure = cgveHead.collateralizedNegative();

		double dblTailCollateralizedNegativeExposure = cgveTail.collateralizedNegative();

		double dblHeadCSA = cgvnHead.csa();

		double dblTailCSA = cgvnTail.csa();

		double dblHeadCollateralizedPositiveExposurePV = dblHeadCollateralizedPositiveExposure / dblHeadCSA;
		double dblTailCollateralizedPositiveExposurePV = dblTailCollateralizedPositiveExposure / dblTailCSA;
		double dblHeadCollateralizedNegativeExposurePV = dblHeadCollateralizedNegativeExposure / dblHeadCSA;
		double dblTailCollateralizedNegativeExposurePV = dblTailCollateralizedNegativeExposure / dblTailCSA;
		double dblEdgeCollateralizedPositiveExposurePV = 0.5 * (dblTailCollateralizedPositiveExposurePV +
			dblHeadCollateralizedPositiveExposurePV);
		double dblEdgeCollateralizedNegativeExposurePV = 0.5 * (dblTailCollateralizedNegativeExposurePV +
			dblHeadCollateralizedNegativeExposurePV);

		try {
			return new org.drip.xva.trajectory.CollateralGroupEdgeAdjustment (
				dblEdgeCollateralizedPositiveExposurePV * (cgvnTail.counterPartySurvival() -
					cgvnHead.counterPartySurvival()) * (1. - dblEdgeCounterPartyRecovery),
				dblEdgeCollateralizedNegativeExposurePV * (cgvnTail.bankSurvival() - cgvnHead.bankSurvival())
					* (1. - dblEdgeBankRecovery),
				0.5 * (
					dblTailCollateralizedPositiveExposurePV * cgvnTail.bankFundingSpread() +
					dblHeadCollateralizedPositiveExposurePV * cgvnHead.bankFundingSpread()
				) * (_cgvTail.vertex().julian() - _cgvHead.vertex().julian()) / 365.25
			);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
