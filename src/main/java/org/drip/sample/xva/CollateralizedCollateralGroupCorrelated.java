
package org.drip.sample.xva;

import org.drip.analytics.date.*;
import org.drip.measure.continuousmarginal.BrokenDateBridgeLinearT;
import org.drip.measure.discretemarginal.SequenceGenerator;
import org.drip.measure.dynamics.*;
import org.drip.measure.process.DiffusionEvolver;
import org.drip.measure.realization.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.linearalgebra.Matrix;
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
 * CollateralizedCollateralGroupCorrelated illustrates the Sample Run of a Single Partially Collateralized
 *  Collateral Group under Non-Zero Bank/Counter Party Threshold with several Fix-Float Swaps, and with built
 *  in Factor Correlations across the Numeraires. The References are:
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

public class CollateralizedCollateralGroupCorrelated {

	private static final double[] ATMSwapRateOffsetRealization (
		final DiffusionEvolver deATMSwapRateOffset,
		final double dblATMSwapRateOffsetStart,
		final int iNumStep,
		final double[] adblRandom,
		final double dblTime,
		final double dblTimeWidth)
		throws Exception
	{
		JumpDiffusionEdge[] aJDEATMSwapRateOffset = deATMSwapRateOffset.incrementSequence (
			new JumpDiffusionVertex (
				dblTime,
				dblATMSwapRateOffsetStart,
				0.,
				false
			),
			UnitRandom.Diffusion (adblRandom),
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
		final double[] adblRandom,
		final double dblTime,
		final double dblTimeWidth,
		final int iNumSwap)
		throws Exception
	{
		double[] adblSwapPortfolioValueRealization = new double[iNumStep];

		for (int i = 0; i < iNumStep; ++i)
			adblSwapPortfolioValueRealization[i] = 0.;

		for (int i = 0; i < iNumSwap; ++i) {
			double[] adblATMSwapRateOffsetRealization = ATMSwapRateOffsetRealization (
				deATMSwapRateOffset,
				dblATMSwapRateOffsetStart,
				iNumStep,
				adblRandom,
				dblTime,
				dblTimeWidth
			);

			for (int j = 0; j < iNumStep; ++j)
				adblSwapPortfolioValueRealization[j] += dblTimeWidth * (iNumStep - j) * adblATMSwapRateOffsetRealization[j];
		}

		return adblSwapPortfolioValueRealization;
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
		double dblATMSwapRateOffsetDrift = 0.0;
		double dblATMSwapRateOffsetVolatility = 0.25;
		double dblATMSwapRateOffsetInitial = 0.;
		double dblCSADrift = 0.01;
		double dblCSAVolatility = 0.05;
		double dblCSAInitial = 1.;
		double dblBankHazardRateDrift = 0.002;
		double dblBankHazardRateVolatility = 0.20;
		double dblBankHazardRateInitial = 0.015;
		double dblBankRecoveryRateDrift = 0.002;
		double dblBankRecoveryRateVolatility = 0.02;
		double dblBankRecoveryRateInitial = 0.40;
		double dblCounterPartyHazardRateDrift = 0.002;
		double dblCounterPartyHazardRateVolatility = 0.30;
		double dblCounterPartyHazardRateInitial = 0.030;
		double dblCounterPartyRecoveryRateDrift = 0.002;
		double dblCounterPartyRecoveryRateVolatility = 0.02;
		double dblCounterPartyRecoveryRateInitial = 0.30;
		double dblBankFundingSpreadDrift = 0.00002;
		double dblBankFundingSpreadVolatility = 0.002;
		double dblBankThreshold = -0.1;
		double dblCounterPartyThreshold = 0.1;

		double[][] aadblCorrelation = new double[][] {
			{1.00, 0.03,  0.07,  0.04,  0.05,  0.08,  0.00},  // PORTFOLIO
			{0.03, 1.00,  0.26,  0.33,  0.21,  0.35,  0.13},  // CSA
			{0.07, 0.26,  1.00,  0.45, -0.17,  0.07,  0.77},  // BANK HAZARD
			{0.04, 0.33,  0.45,  1.00, -0.22, -0.54,  0.58},  // COUNTER PARTY HAZARD
			{0.05, 0.21, -0.17, -0.22,  1.00,  0.47, -0.23},  // BANK RECOVERY
			{0.08, 0.35,  0.07, -0.54,  0.47,  1.00,  0.01},  // COUNTER PARTY RECOVERY
			{0.00, 0.13,  0.77,  0.58, -0.23,  0.01,  1.00}   // BANK FUNDING SPREAD
		};

		JulianDate dtSpot = DateUtil.Today();

		double dblTimeWidth = dblTime / iNumStep;
		JulianDate[] adtVertex = new JulianDate[iNumStep];
		double[][] aadblPortfolioValue = new double[iNumSimulation][iNumStep];
		double[][] aadblCollateralBalance = new double[iNumSimulation][iNumStep];
		double dblBankFundingSpreadInitial = dblBankHazardRateInitial / (1. - dblBankRecoveryRateInitial);
		CollateralGroupVertexNumeraire[][] aaCGVN = new CollateralGroupVertexNumeraire[iNumSimulation][iNumStep];

		CollateralGroupSpecification cgs = CollateralGroupSpecification.FixedThreshold (
			"FIXEDTHRESHOLD",
			dblCounterPartyThreshold,
			dblBankThreshold
		);

		CounterPartyGroupSpecification cpgs = CounterPartyGroupSpecification.Standard ("CPGROUP");

		for (int j = 0; j < iNumStep; ++j)
			adtVertex[j] = dtSpot.addMonths (6 * j + 6);

		DiffusionEvolver deATMSwapRateOffset = new DiffusionEvolver (
			DiffusionEvaluatorLinear.Standard (
				dblATMSwapRateOffsetDrift,
				dblATMSwapRateOffsetVolatility
			)
		);

		DiffusionEvolver deCSA = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblCSADrift,
				dblCSAVolatility
			)
		);

		DiffusionEvolver deBankHazardRate = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblBankHazardRateDrift,
				dblBankHazardRateVolatility
			)
		);

		DiffusionEvolver deCounterPartyHazardRate = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblCounterPartyHazardRateDrift,
				dblCounterPartyHazardRateVolatility
			)
		);

		DiffusionEvolver deBankRecoveryRate = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblBankRecoveryRateDrift,
				dblBankRecoveryRateVolatility
			)
		);

		DiffusionEvolver deCounterPartyRecoveryRate = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblCounterPartyRecoveryRateDrift,
				dblCounterPartyRecoveryRateVolatility
			)
		);

		DiffusionEvolver deBankFundingSpread = new DiffusionEvolver (
			DiffusionEvaluatorLinear.Standard (
				dblBankFundingSpreadDrift,
				dblBankFundingSpreadVolatility
			)
		);

		for (int i = 0; i < iNumSimulation; ++i) {
			double[][] aadblNumeraire = Matrix.Transpose (
				SequenceGenerator.GaussianJoint (
					iNumStep,
					aadblCorrelation
				)
			);

			aadblPortfolioValue[i] = SwapPortfolioValueRealization (
				deATMSwapRateOffset,
				dblATMSwapRateOffsetInitial,
				iNumStep,
				aadblNumeraire[0],
				dblTime,
				dblTimeWidth,
				iNumSwap
			);

			JumpDiffusionEdge[] aJDECSA = deCSA.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblCSAInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (aadblNumeraire[1]),
				dblTimeWidth
			);

			JumpDiffusionEdge[] aJDEBankHazardRate = deBankHazardRate.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblBankHazardRateInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (aadblNumeraire[2]),
				dblTimeWidth
			);

			JumpDiffusionEdge[] aJDECounterPartyHazardRate = deCounterPartyHazardRate.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblCounterPartyHazardRateInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (aadblNumeraire[3]),
				dblTimeWidth
			);

			JumpDiffusionEdge[] aJDEBankRecoveryRate = deBankRecoveryRate.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblBankRecoveryRateInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (aadblNumeraire[4]),
				dblTimeWidth
			);

			JumpDiffusionEdge[] aJDECounterPartyRecoveryRate = deCounterPartyRecoveryRate.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblCounterPartyRecoveryRateInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (aadblNumeraire[5]),
				dblTimeWidth
			);

			JumpDiffusionEdge[] aJDEBankFundingSpread = deBankFundingSpread.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblBankFundingSpreadInitial,
					0.,
					false
				),
				UnitRandom.Diffusion (aadblNumeraire[6]),
				dblTimeWidth
			);

			JulianDate dtStart = dtSpot;
			double dblValueStart = dblTime * dblATMSwapRateOffsetInitial;

			for (int j = 0; j < iNumStep; ++j) {
				JulianDate dtEnd = adtVertex[j];
				double dblValueEnd = aadblPortfolioValue[i][j];

				aaCGVN[i][j] = new CollateralGroupVertexNumeraire (
					aJDECSA[j].finish(),
					Math.exp (-0.5 * aJDEBankHazardRate[j].finish() * (j + 1)),
					aJDEBankRecoveryRate[j].finish(),
					aJDEBankFundingSpread[j].finish(),
					Math.exp (-0.5 * aJDECounterPartyHazardRate[j].finish() * (j + 1)),
					aJDECounterPartyRecoveryRate[j].finish()
				);

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

				aadblCollateralBalance[i][j] = cae.postingRequirement (dtEnd);

				dblValueStart = dblValueEnd;
				dtStart = dtEnd;
			}
		}

		NettingGroupPathAggregator ngpa = NettingGroupPathAggregator.Standard (
			adtVertex,
			aadblPortfolioValue,
			aadblCollateralBalance,
			aaCGVN
		);

		JulianDate[] adtVertexNode = ngpa.vertexes();

		System.out.println();

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		String strDump = "\t|         DATE         =>" ;

		for (int i = 0; i < adtVertexNode.length; ++i)
			strDump = strDump + " " + adtVertexNode[i] + " |";

		System.out.println (strDump);

		System.out.println ("\t|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		double[] adblEE = ngpa.collateralizedExposure();

		strDump = "\t|       EXPOSURE       =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetInitial, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPE = ngpa.collateralizedPositiveExposure();

		strDump = "\t|  POSITIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetInitial, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEPE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEPE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblENE = ngpa.collateralizedNegativeExposure();

		strDump = "\t|  NEGATIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (0., 1, 4, 1.) + "   |";

		for (int j = 0; j < adblENE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblENE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEEPV = ngpa.collateralizedExposurePV();

		strDump = "\t|      EXPOSURE PV     =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetInitial, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPEPV = ngpa.collateralizedPositiveExposurePV();

		strDump = "\t| POSITIVE EXPOSURE PV =>   " + FormatUtil.FormatDouble (dblTime * dblATMSwapRateOffsetInitial, 1, 4, 1.) + "   |";

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
