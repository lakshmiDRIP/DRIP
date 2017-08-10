
package org.drip.sample.xva;

import org.drip.analytics.date.*;
import org.drip.measure.continuousmarginal.BrokenDateBridgeLinearT;
import org.drip.measure.discretemarginal.SequenceGenerator;
import org.drip.measure.dynamics.DiffusionEvaluatorLinear;
import org.drip.measure.process.DiffusionEvolver;
import org.drip.measure.realization.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.env.EnvManager;
import org.drip.xva.settings.*;
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
 * CollateralizedCollateralGroup illustrates the Sample Run of a Single Partially Collateralized Collateral
 *  Group under Non-Zero Bank/Counter Party Threshold with several Fix-Float Swaps. The References are:
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

public class CollateralizedCollateralGroup {

	private static final double[] ATMSwapRateOffsetRealization (
		final DiffusionEvolver deSwapATMSwapRateOffset,
		final double dblATMSwapRateOffsetStart,
		final int iNumStep,
		final double dblTime,
		final double dblTimeWidth)
		throws Exception
	{
		JumpDiffusionEdge[] aJDEATMSwapRateOffset = deSwapATMSwapRateOffset.incrementSequence (
			new JumpDiffusionVertex (
				dblTime,
				dblATMSwapRateOffsetStart,
				0.,
				false
			),
			UnitRandom.Diffusion (SequenceGenerator.Gaussian (iNumStep)),
			dblTimeWidth
		);

		double[] adblATMSwapRateOffsetRealization = new double[aJDEATMSwapRateOffset.length];

		for (int i = 0; i < aJDEATMSwapRateOffset.length; ++i)
			adblATMSwapRateOffsetRealization[i] = aJDEATMSwapRateOffset[i].finish();

		return adblATMSwapRateOffsetRealization;
	}

	private static final double[] SwapPortfolioValueRealization (
		final DiffusionEvolver deATMSwapRateOffset,
		final double dblATMSwapRateOffsetStart,
		final int iNumStep,
		final double dblTime,
		final double dblTimeWidth,
		final int iNumSwap)
		throws Exception
	{
		double[] adblSwapPortfolioValueRealization = new double[iNumStep];

		for (int i = 0; i < iNumStep; ++i)
			adblSwapPortfolioValueRealization[i] = 0.;

		for (int i = 0; i < iNumSwap; ++i) {
			double[] adblSwapValueRealization = ATMSwapRateOffsetRealization (
				deATMSwapRateOffset,
				dblATMSwapRateOffsetStart,
				iNumStep,
				dblTime,
				dblTimeWidth
			);

			for (int j = 0; j < iNumStep; ++j)
				adblSwapPortfolioValueRealization[j] += dblTime * (iNumStep - j) * adblSwapValueRealization[j];
		}

		return adblSwapPortfolioValueRealization;
	}

	private static final double[][] SwapPortfolioValueRealization (
		final DiffusionEvolver deSwap,
		final double dblSwapValueStart,
		final int iNumStep,
		final double dblTime,
		final double dblTimeWidth,
		final int iNumSwap,
		final int iNumSimulation)
		throws Exception
	{
		double[][] aadblSwapPortfolioValueRealization = new double[iNumSimulation][];

		for (int i = 0; i < iNumSimulation; ++i)
			aadblSwapPortfolioValueRealization[i] = SwapPortfolioValueRealization (
				deSwap,
				dblSwapValueStart,
				iNumStep,
				dblTime,
				dblTimeWidth,
				iNumSwap
			);

		return aadblSwapPortfolioValueRealization;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		EnvManager.InitEnv ("");

		int iNumStep = 10;
		int iNumSwap = 10;
		double dblTime = 5.;
		int iNumSimulation = 1000;
		double dblATMSwapRateOffsetStart = 0.;
		double dblATMSwapRateOffsetDrift = 0.0;
		double dblATMSwapRateOffsetVolatility = 0.25;
		double dblCSADrift = 0.01;
		double dblBankHazardRate = 0.015;
		double dblBankRecoveryRate = 0.40;
		double dblCounterPartyHazardRate = 0.030;
		double dblCounterPartyRecoveryRate = 0.30;
		double dblBankThreshold = -0.1;
		double dblCounterPartyThreshold = 0.1;

		JulianDate dtSpot = DateUtil.Today();

		double dblTimeWidth = dblTime / iNumStep;
		double[] adblCSA = new double[iNumStep];
		double[] adblBankSurvival = new double[iNumStep];
		double[] adblBankRecovery = new double[iNumStep];
		JulianDate[] adtVertex = new JulianDate[iNumStep];
		double[] adblBankFundingSpread = new double[iNumStep];
		double[] adblCounterPartySurvival = new double[iNumStep];
		double[] adblCounterPartyRecovery = new double[iNumStep];
		CollateralGroupPath[] aCGP = new CollateralGroupPath[iNumSimulation];
		double dblBankFundingSpread = dblBankHazardRate / (1. - dblBankRecoveryRate);
		CollateralGroupVertex[][] aaCGV = new CollateralGroupVertex[iNumSimulation][iNumStep];

		CollateralGroupSpecification cgs = CollateralGroupSpecification.FixedThreshold (
			"FIXEDTHRESHOLD",
			dblCounterPartyThreshold,
			dblBankThreshold
		);

		CounterPartyGroupSpecification cpgs = CounterPartyGroupSpecification.Standard ("CPGROUP");

		DiffusionEvolver deSwapATMSwapRateOffset = new DiffusionEvolver (
			DiffusionEvaluatorLinear.Standard (
				dblATMSwapRateOffsetDrift,
				dblATMSwapRateOffsetVolatility
			)
		);

		double[][] aadblSwapPortfolioValueRealization = SwapPortfolioValueRealization (
			deSwapATMSwapRateOffset,
			dblATMSwapRateOffsetStart,
			iNumStep,
			dblTime,
			dblTimeWidth,
			iNumSwap,
			iNumSimulation
		);

		for (int i = 0; i < iNumStep; ++i) {
			adblBankRecovery[i] = dblBankRecoveryRate;
			adblBankFundingSpread[i] = dblBankFundingSpread;
			adblCounterPartyRecovery[i] = dblCounterPartyRecoveryRate;

			adtVertex[i] = dtSpot.addMonths (((int) dblTime) * 12 * (i + 1) / iNumStep);

			adblCSA[i] = Math.exp (0.5 * dblCSADrift * (i + 1));

			adblBankSurvival[i] = Math.exp (-0.5 * dblBankHazardRate * (i + 1));

			adblCounterPartySurvival[i] = Math.exp (-0.5 * dblCounterPartyHazardRate * (i + 1));
		}

		for (int j = 0; j < iNumSimulation; ++j) {
			JulianDate dtStart = dtSpot;
			double dblValueStart = dblTime * dblATMSwapRateOffsetStart;

			for (int i = 0; i < iNumStep; ++i) {
				JulianDate dtEnd = adtVertex[i];
				double dblValueEnd = dblTimeWidth * (iNumStep - i) * aadblSwapPortfolioValueRealization[j][i];

				CollateralAmountEstimator cae = new CollateralAmountEstimator (
					cgs,
					cpgs,
					new BrokenDateBridgeLinearT (
						dtStart.julian(),
						dtEnd.julian(),
						dblValueStart,
						dblValueEnd
					),
					Double.NaN
				);

				aaCGV[j][i] = new CollateralGroupVertex (
					adtVertex[i],
					new CollateralGroupVertexExposure (
						dblValueEnd,
						0.,
						cae.postingRequirement (dtEnd)
					),
					new CollateralGroupVertexNumeraire (
						adblCSA[i],
						adblBankSurvival[i],
						adblBankRecovery[i],
						adblBankFundingSpread[i],
						adblCounterPartySurvival[i],
						adblCounterPartyRecovery[i]
					)
				);

				dtStart = dtEnd;
				dblValueStart = dblValueEnd;
			}
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

		JulianDate[] adtVertexNode = ngpa.vertexes();

		System.out.println();

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		String strDump = "\t|         DATE         =>" ;

		for (int i = 0; i < adtVertexNode.length; ++i)
			strDump = strDump + " " + adtVertexNode[i] + " |";

		System.out.println (strDump);

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		double[] adblEE = ngpa.collateralizedExposure();

		strDump = "\t|       EXPOSURE       =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetStart, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPE = ngpa.collateralizedPositiveExposure();

		strDump = "\t|  POSITIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetStart, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEPE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEPE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblENE = ngpa.collateralizedNegativeExposure();

		strDump = "\t|  NEGATIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (0., 1, 4, 1.) + "   |";

		for (int j = 0; j < adblENE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblENE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEEPV = ngpa.collateralizedExposurePV();

		strDump = "\t|      EXPOSURE PV     =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetStart, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPEPV = ngpa.collateralizedPositiveExposurePV();

		strDump = "\t| POSITIVE EXPOSURE PV =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetStart, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEPEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEPEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblENEPV = ngpa.collateralizedNegativeExposurePV();

		strDump = "\t| NEGATIVE EXPOSURE PV =>   " + FormatUtil.FormatDouble (0., 1, 4, 1.) + "   |";

		for (int j = 0; j < adblENEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblENEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		System.out.println();

		System.out.println ("\t||----------------||");

		System.out.println ("\t|| CVA => " + FormatUtil.FormatDouble (ngpa.cva(), 2, 2, 100.) + "% ||");

		System.out.println ("\t|| DVA => " + FormatUtil.FormatDouble (ngpa.dva(), 2, 2, 100.) + "% ||");

		System.out.println ("\t|| FVA => " + FormatUtil.FormatDouble (ngpa.fca(), 2, 2, 100.) + "% ||");

		System.out.println ("\t||----------------||");

		System.out.println();
	}
}
