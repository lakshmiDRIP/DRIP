
package org.drip.sample.burgard2012;

import org.drip.analytics.date.*;
import org.drip.measure.discretemarginal.SequenceGenerator;
import org.drip.measure.dynamics.DiffusionEvaluatorLinear;
import org.drip.measure.process.DiffusionEvolver;
import org.drip.measure.realization.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.env.EnvManager;
import org.drip.xva.trajectory.*;

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
 * FixFloatVABank illustrates the Fix-Float Swap Valuation Adjustment Metrics Dependence on the Bank Spread
 *  using the Set of Netting Group Exposure Simulations. The References are:
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

public class FixFloatVABank {

	private static final JumpDiffusionEdge[][] ATMSwapRateOffsetRealization (
		final DiffusionEvolver deATMSwapRateOffset,
		final double dblATMSwapRateOffsetInitial,
		final double dblTime,
		final double dblTimeWidth,
		final int iNumStep,
		final int iNumSimulation)
		throws Exception
	{
		JumpDiffusionEdge[][] aaJDEATMSwapRateOffset = new JumpDiffusionEdge[iNumSimulation][];

		for (int i = 0; i < iNumSimulation; ++i)
			aaJDEATMSwapRateOffset[i] = deATMSwapRateOffset.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblATMSwapRateOffsetInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (SequenceGenerator.Gaussian (iNumStep)),
				dblTimeWidth
			);

		return aaJDEATMSwapRateOffset;
	}

	public static final void VA (
		final double dblBankHazardRate)
		throws Exception
	{
		int iNumStep = 10;
		double dblTime = 5.;
		int iNumSimulation = 10000;
		double dblATMSwapRateOffsetDrift = 0.0;
		double dblATMSwapRateOffsetVolatility = 0.15;
		double dblATMSwapRateOffsetInitial = 0.;
		double dblCollateralDrift = 0.01;
		double dblBankRecoveryRate = 0.40;
		double dblCounterPartyHazardRate = 0.015;
		double dblCounterPartyRecoveryRate = 0.30;

		double dblTimeWidth = dblTime / iNumStep;
		double[] adblCollateral = new double[iNumStep];
		double[] adblBankSurvival = new double[iNumStep];
		double[] adblBankRecovery = new double[iNumStep];
		JulianDate[] adtVertex = new JulianDate[iNumStep];
		double[] adblBankFundingSpread = new double[iNumStep];
		double[] adblCounterPartySurvival = new double[iNumStep];
		double[] adblCounterPartyRecovery = new double[iNumStep];
		CollateralGroupPath[] aCGP = new CollateralGroupPath[iNumSimulation];
		double dblBankFundingSpread = dblBankHazardRate / (1. - dblBankRecoveryRate);
		CollateralGroupVertex[][] aaCGV = new CollateralGroupVertex[iNumSimulation][iNumStep];

		JulianDate dtSpot = DateUtil.Today();

		JumpDiffusionEdge[][] aaJDEATMSwapRateOffset = ATMSwapRateOffsetRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLinear.Standard (
					dblATMSwapRateOffsetDrift,
					dblATMSwapRateOffsetVolatility
				)
			),
			dblATMSwapRateOffsetInitial,
			dblTime,
			dblTimeWidth,
			iNumStep,
			iNumSimulation
		);

		for (int i = 0; i < iNumStep; ++i) {
			adblBankRecovery[i] = dblBankRecoveryRate;
			adblBankFundingSpread[i] = dblBankFundingSpread;
			adblCounterPartyRecovery[i] = dblCounterPartyRecoveryRate;

			adtVertex[i] = dtSpot.addMonths (6 * i + 6);

			adblCollateral[i] = Math.exp (0.5 * dblCollateralDrift * (i + 1));

			adblBankSurvival[i] = Math.exp (-0.5 * dblBankHazardRate * (i + 1));

			adblCounterPartySurvival[i] = Math.exp (-0.5 * dblCounterPartyHazardRate * (i + 1));
		}

		for (int i = 0; i < iNumStep; ++i) {
			for (int j = 0; j < iNumSimulation; ++j)
				aaCGV[j][i] = new CollateralGroupVertex (
					adtVertex[i],
					new CollateralGroupVertexExposure (
						aaJDEATMSwapRateOffset[j][i].finish() * dblTimeWidth * (iNumStep - i),
						0.,
						0.
					),
					new CollateralGroupVertexNumeraire (
						adblCollateral[i],
						adblBankSurvival[i],
						adblBankRecovery[i],
						adblBankFundingSpread[i],
						adblCounterPartySurvival[i],
						adblCounterPartyRecovery[i]
					)
				);
		}

		for (int j = 0; j < iNumSimulation; ++j) {
			CollateralGroupEdge[] aCGE = new CollateralGroupEdge[iNumStep - 1];

			for (int i = 1; i < iNumStep; ++i)
				aCGE[i - 1] = new CollateralGroupEdge (
					aaCGV[j][i - 1],
					aaCGV[j][i]
				);

			aCGP[j] = new CollateralGroupPath (aCGE);
		}

		NettingGroupPathAggregator ngpa = NettingGroupPathAggregator.Standard (aCGP);

		System.out.println ("\t|| " +
			FormatUtil.FormatDouble (dblBankHazardRate, 3, 0, 10000.) + " bp => " +
			FormatUtil.FormatDouble (ngpa.cva(), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (ngpa.dva(), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (ngpa.fca(), 1, 2, 100.) + "% | " +
			FormatUtil.FormatDouble (ngpa.total(), 1, 2, 100.) + "% ||"
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		EnvManager.InitEnv ("");

		double[] adblBankHazardRate = new double[] {
			0.0025,
			0.0050,
			0.0075,
			0.0100,
			0.0125,
			0.0150,
			0.0175,
			0.0200,
			0.0225,
			0.0250,
			0.0275,
			0.0300
		};

		System.out.println();

		System.out.println ("\t||----------------------------------------------||");

		System.out.println ("\t||       VA DEPENDENCE ON BANK HAZARD RATE      ||");

		System.out.println ("\t||----------------------------------------------||");

		System.out.println ("\t||  Hazard =>   CVA  |   DVA  |   FCA  |  Total ||");

		System.out.println ("\t||----------------------------------------------||");

		for (double dblBankHazardRate : adblBankHazardRate)
			VA (dblBankHazardRate);

		System.out.println ("\t||----------------------------------------------||");

		System.out.println();
	}
}
