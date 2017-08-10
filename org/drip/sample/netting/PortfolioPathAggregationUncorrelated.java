
package org.drip.sample.netting;

import org.drip.analytics.date.*;
import org.drip.measure.discretemarginal.SequenceGenerator;
import org.drip.measure.dynamics.*;
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
 * PortfolioPathAggregationUncorrelated generates the Aggregation of the Portfolio Paths evolved using
 * 	Uncorrelated Market Parameters. The References are:
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

public class PortfolioPathAggregationUncorrelated {

	private static final JumpDiffusionEdge[] PathRealization (
		final DiffusionEvolver de,
		final double dblInitialValue,
		final double dblTime,
		final double dblTimeWidth,
		final int iNumStep)
		throws Exception
	{
		return de.incrementSequence (
			new JumpDiffusionVertex (
				dblTime,
				dblInitialValue,
				0.,
				false
			),
			UnitRandom.Diffusion (SequenceGenerator.Gaussian (iNumStep)),
			dblTimeWidth
		);
	}

	private static final double[][] PortfolioRealization (
		final DiffusionEvolver dePortfolio,
		final double dblInitialAssetValue,
		final double dblTime,
		final double dblTimeWidth,
		final int iNumStep,
		final int iNumSimulation)
		throws Exception
	{
		double[][] aadblPortfolioValue = new double[iNumSimulation][];

		for (int i = 0; i < iNumSimulation; ++i) {
			JumpDiffusionEdge[] aJDE = dePortfolio.incrementSequence (
				new JumpDiffusionVertex (
					dblTime,
					dblInitialAssetValue,
					0.,
					false
				),
				UnitRandom.Diffusion (SequenceGenerator.Gaussian (iNumStep)),
				dblTimeWidth
			);

			aadblPortfolioValue[i] = new double[aJDE.length];

			for (int j = 0; j < aJDE.length; ++j)
				aadblPortfolioValue[i][j] = aJDE[j].finish();
		}

		return aadblPortfolioValue;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		EnvManager.InitEnv ("");

		int iNumStep = 10;
		double dblTime = 5.;
		int iNumSimulation = 100000;
		double dblAssetDrift = 0.06;
		double dblAssetVolatility = 0.15;
		double dblAssetInitial = 1.;
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

		double dblTimeWidth = dblTime / iNumStep;
		JulianDate[] adtVertex = new JulianDate[iNumStep];
		double[][] aadblCollateralBalance = new double[iNumSimulation][iNumStep];
		CollateralGroupVertexNumeraire[] aCGVN = new CollateralGroupVertexNumeraire[iNumStep];
		double dblBankFundingSpreadInitial = dblBankHazardRateInitial / (1. - dblBankRecoveryRateInitial);

		JulianDate dtSpot = DateUtil.Today();

		double[][] aadblPortfolioValue = PortfolioRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLogarithmic.Standard (
					dblAssetDrift,
					dblAssetVolatility
				)
			),
			dblAssetInitial,
			dblTime,
			dblTimeWidth,
			iNumStep,
			iNumSimulation
		);

		JumpDiffusionEdge[] aJDECollateral = PathRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLogarithmic.Standard (
					dblCSADrift,
					dblCSAVolatility
				)
			),
			dblCSAInitial,
			dblTime,
			dblTimeWidth,
			iNumStep
		);

		JumpDiffusionEdge[] aJDEBankHazardRate = PathRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLogarithmic.Standard (
					dblBankHazardRateDrift,
					dblBankHazardRateVolatility
				)
			),
			dblBankHazardRateInitial,
			dblTime,
			dblTimeWidth,
			iNumStep
		);

		JumpDiffusionEdge[] aJDEBankRecoveryRate = PathRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLogarithmic.Standard (
					dblBankRecoveryRateDrift,
					dblBankRecoveryRateVolatility
				)
			),
			dblBankRecoveryRateInitial,
			dblTime,
			dblTimeWidth,
			iNumStep
		);

		JumpDiffusionEdge[] aJDECounterPartyHazardRate = PathRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLogarithmic.Standard (
					dblCounterPartyHazardRateDrift,
					dblCounterPartyHazardRateVolatility
				)
			),
			dblCounterPartyHazardRateInitial,
			dblTime,
			dblTimeWidth,
			iNumStep
		);

		JumpDiffusionEdge[] aJDECounterPartyRecoveryRate = PathRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLogarithmic.Standard (
					dblCounterPartyRecoveryRateDrift,
					dblCounterPartyRecoveryRateVolatility
				)
			),
			dblCounterPartyRecoveryRateInitial,
			dblTime,
			dblTimeWidth,
			iNumStep
		);

		JumpDiffusionEdge[] aJDEBankFundingSpread = PathRealization (
			new DiffusionEvolver (
				DiffusionEvaluatorLinear.Standard (
					dblBankFundingSpreadDrift,
					dblBankFundingSpreadVolatility
				)
			),
			dblBankFundingSpreadInitial,
			dblTime,
			dblTimeWidth,
			iNumStep
		);

		for (int i = 0; i < iNumStep; ++i) {
			adtVertex[i] = dtSpot.addMonths (6 * i + 6);

			aCGVN[i] = new CollateralGroupVertexNumeraire (
				aJDECollateral[i].finish(),
				Math.exp (-0.5 * aJDEBankHazardRate[i].finish() * (i + 1)),
				aJDEBankRecoveryRate[i].finish(),
				aJDEBankFundingSpread[i].finish(),
				Math.exp (-0.5 * aJDECounterPartyHazardRate[i].finish() * (i + 1)),
				aJDECounterPartyRecoveryRate[i].finish()
			);

			for (int j = 0; j < iNumSimulation; ++j)
				aadblCollateralBalance[j][i] = 0.;
		}

		NettingGroupPathAggregator ngpa = NettingGroupPathAggregator.Standard (
			adtVertex,
			aadblPortfolioValue,
			aadblCollateralBalance,
			aCGVN
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

		strDump = "\t|       EXPOSURE       =>   " + FormatUtil.FormatDouble (dblAssetInitial, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPE = ngpa.collateralizedPositiveExposure();

		strDump = "\t|  POSITIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (dblAssetInitial, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEPE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEPE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblENE = ngpa.collateralizedNegativeExposure();

		strDump = "\t|  NEGATIVE EXPOSURE   =>   " + FormatUtil.FormatDouble (0., 1, 4, 1.) + "   |";

		for (int j = 0; j < adblENE.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblENE[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEEPV = ngpa.collateralizedExposurePV();

		strDump = "\t|      EXPOSURE PV     =>   " + FormatUtil.FormatDouble (dblAssetInitial, 1, 4, 1.) + "   |";

		for (int j = 0; j < adblEEPV.length; ++j)
			strDump = strDump + "   " + FormatUtil.FormatDouble (adblEEPV[j], 1, 4, 1.) + "   |";

		System.out.println (strDump);

		double[] adblEPEPV = ngpa.collateralizedPositiveExposurePV();

		strDump = "\t| POSITIVE EXPOSURE PV =>   " + FormatUtil.FormatDouble (dblAssetInitial, 1, 4, 1.) + "   |";

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
