
package org.drip.sample.burgard2011;

import org.drip.measure.dynamics.DiffusionEvaluatorLogarithmic;
import org.drip.measure.process.DiffusionEvolver;
import org.drip.measure.realization.JumpDiffusionVertex;
import org.drip.quant.common.FormatUtil;
import org.drip.service.env.EnvManager;
import org.drip.xva.definition.*;
import org.drip.xva.derivative.*;
import org.drip.xva.pde.*;
import org.drip.xva.settings.PDEEvolutionControl;

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
 * XVAGreeks demonstrates the Bank and Counter-Party Default Based Derivative Evolution of the XVA Greeks and
 *  their Components. The References are:
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

public class XVAGreeks {

	private static final EdgeEvolutionTrajectory RunStep (
		final TrajectoryEvolutionScheme tes,
		final SpreadIntensity si,
		final BurgardKjaerOperator bko,
		final EdgeEvolutionTrajectory eetStart)
		throws Exception
	{
		EdgeAssetGreek eagStart = eetStart.edgeAssetGreek();

		EdgeReplicationPortfolio erpStart = eetStart.replicationPortfolio();

		double dblDerivativeXVAValueStart = eagStart.derivativeXVAValue();

		double dblTimeWidth = tes.timeIncrement();

		double dblTimeStart = eetStart.time();

		double dblTime = dblTimeStart - 0.5 * dblTimeWidth;

		UniverseSnapshot usStart = eetStart.tradeableAssetSnapshot();

		TwoWayRiskyUniverse twru = tes.universe();

		double dblCollateralBondNumeraire = usStart.zeroCouponCollateralBondNumeraire().finish();

		UniverseSnapshot usFinish = new UniverseSnapshot (
			twru.asset().priceNumeraire().weinerIncrement (
				new JumpDiffusionVertex (
					dblTime,
					usStart.assetNumeraire().finish(),
					0.,
					false
				),
				dblTimeWidth
			),
			twru.zeroCouponCollateralBond().priceNumeraire().weinerIncrement (
				new JumpDiffusionVertex (
					dblTime,
					dblCollateralBondNumeraire,
					0.,
					false
				),
				dblTimeWidth
			),
			twru.zeroCouponBankBond().priceNumeraire().weinerIncrement (
				new JumpDiffusionVertex (
					dblTime,
					usStart.zeroCouponBankBondNumeraire().finish(),
					0.,
					false
				),
				dblTimeWidth
			),
			twru.zeroCouponCounterPartyBond().priceNumeraire().weinerIncrement (
				new JumpDiffusionVertex (
					dblTime,
					usStart.zeroCouponCounterPartyBondNumeraire().finish(),
					0.,
					false
				),
				dblTimeWidth
			)
		);

		MasterAgreementCloseOut maco = tes.boundaryCondition();

		LevelBurgardKjaerRun lbkr = bko.timeIncrementRun (
			si,
			eetStart
		);

		double dblTheta = lbkr.theta();

		double dblAssetNumeraireBump = lbkr.assetNumeraireBump();

		double dblThetaAssetNumeraireUp = lbkr.thetaAssetNumeraireUp();

		double dblThetaAssetNumeraireDown = lbkr.thetaAssetNumeraireDown();

		double dblDerivativeXVAValueDeltaFinish = eagStart.derivativeXVAValueDelta() +
			0.5 * (dblThetaAssetNumeraireUp - dblThetaAssetNumeraireDown) * dblTimeWidth / dblAssetNumeraireBump;

		double dblDerivativeXVAValueGammaFinish = eagStart.derivativeXVAValueGamma() +
			(dblThetaAssetNumeraireUp + dblThetaAssetNumeraireDown - 2. * dblTheta) * dblTimeWidth /
				(dblAssetNumeraireBump * dblAssetNumeraireBump);

		double dblDerivativeXVAValueFinish = dblDerivativeXVAValueStart - dblTheta * dblTimeWidth;

		double dblGainOnBankDefaultFinish = -1. * (dblDerivativeXVAValueFinish - maco.bankDefault
			(dblDerivativeXVAValueFinish));

		double dblGainOnCounterPartyDefaultFinish = -1. * (dblDerivativeXVAValueFinish - maco.counterPartyDefault
			(dblDerivativeXVAValueFinish));

		System.out.println ("\t||" +
			FormatUtil.FormatDouble (dblTime, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblDerivativeXVAValueFinish, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblDerivativeXVAValueDeltaFinish, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblDerivativeXVAValueGammaFinish, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblGainOnBankDefaultFinish, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblGainOnCounterPartyDefaultFinish, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (lbkr.derivativeXVAStochasticGrowth(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (lbkr.derivativeXVACollateralGrowth(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (lbkr.derivativeXVAFundingGrowth(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (lbkr.derivativeXVABankDefaultGrowth(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (lbkr.derivativeXVACounterPartyDefaultGrowth(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblThetaAssetNumeraireDown, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblTheta, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblThetaAssetNumeraireUp, 1, 6, 1.) + " ||"
		);

		org.drip.xva.derivative.LevelCashAccount lca = tes.rebalanceCash (
			eetStart,
			usFinish
		).cashAccount();

		return new EdgeEvolutionTrajectory (
			dblTimeStart - dblTimeWidth,
			usFinish,
			new EdgeReplicationPortfolio (
				-1. * dblDerivativeXVAValueDeltaFinish,
				dblGainOnBankDefaultFinish / usFinish.zeroCouponBankBondNumeraire().finish(),
				dblGainOnCounterPartyDefaultFinish / usFinish.zeroCouponCounterPartyBondNumeraire().finish(),
				erpStart.cashAccount() + lca.accumulation()
			),
			new EdgeAssetGreek (
				dblDerivativeXVAValueFinish,
				dblDerivativeXVAValueDeltaFinish,
				dblDerivativeXVAValueGammaFinish,
				eagStart.derivativeValue() * Math.exp (
					-1. * dblTimeWidth * twru.zeroCouponCollateralBond().priceNumeraire().evaluator().drift().value (
						new JumpDiffusionVertex (
							dblTime,
							dblCollateralBondNumeraire,
							0.,
							false
						)
					)
				)
			),
			dblGainOnBankDefaultFinish,
			dblGainOnCounterPartyDefaultFinish
		);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		EnvManager.InitEnv ("");

		double dblSensitivityShiftFactor = 0.001;
		double dblBankRecovery = 0.4;
		double dblCounterPartyRecovery = 0.4;
		double dblAssetDrift = 0.06;
		double dblAssetVolatility = 0.15;
		double dblAssetRepo = 0.03;
		double dblAssetDividend = 0.02;
		double dblZeroCouponCollateralBondDrift = 0.01;
		double dblZeroCouponCollateralBondVolatility = 0.05;
		double dblZeroCouponCollateralBondRepo = 0.005;
		double dblZeroCouponBankBondDrift = 0.03;
		double dblZeroCouponBankBondVolatility = 0.10;
		double dblZeroCouponBankBondRepo = 0.028;
		double dblZeroCouponCounterPartyBondDrift = 0.03;
		double dblZeroCouponCounterPartyBondVolatility = 0.10;
		double dblZeroCouponCounterPartyBondRepo = 0.028;
		double dblTimeWidth = 1. / 24.;
		double dblTime = 1.;
		double dblTerminalXVADerivativeValue = 1.;

		PDEEvolutionControl settings = new PDEEvolutionControl (
			PDEEvolutionControl.CLOSEOUT_GREGORY_LI_TANG,
			dblSensitivityShiftFactor
		);

		MasterAgreementCloseOut maco = new MasterAgreementCloseOut (
			dblBankRecovery,
			dblCounterPartyRecovery
		);

		DiffusionEvolver meAsset = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblAssetDrift,
				dblAssetVolatility
			)
		);

		DiffusionEvolver meZeroCouponCollateralBond = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblZeroCouponCollateralBondDrift,
				dblZeroCouponCollateralBondVolatility
			)
		);

		DiffusionEvolver meZeroCouponBankBond = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblZeroCouponBankBondDrift,
				dblZeroCouponBankBondVolatility
			)
		);

		DiffusionEvolver meZeroCouponCounterPartyBond = new DiffusionEvolver (
			DiffusionEvaluatorLogarithmic.Standard (
				dblZeroCouponCounterPartyBondDrift,
				dblZeroCouponCounterPartyBondVolatility
			)
		);

		TwoWayRiskyUniverse twru = new TwoWayRiskyUniverse (
			new Equity (
				meAsset,
				dblAssetRepo,
				dblAssetDividend
			),
			new Tradeable (
				meZeroCouponCollateralBond,
				dblZeroCouponCollateralBondRepo
			),
			new Tradeable (
				meZeroCouponBankBond,
				dblZeroCouponBankBondRepo
			),
			new Tradeable (
				meZeroCouponCounterPartyBond,
				dblZeroCouponCounterPartyBondRepo
			)
		);

		TrajectoryEvolutionScheme tes = new TrajectoryEvolutionScheme (
			twru,
			maco,
			settings,
			dblTimeWidth
		);

		BurgardKjaerOperator bko = new BurgardKjaerOperator (
			twru,
			maco,
			settings
		);

		SpreadIntensity si = new SpreadIntensity (
			dblZeroCouponBankBondDrift - dblZeroCouponCollateralBondDrift,
			(dblZeroCouponBankBondDrift - dblZeroCouponCollateralBondDrift) / dblBankRecovery,
			(dblZeroCouponCounterPartyBondDrift - dblZeroCouponCollateralBondDrift) / dblCounterPartyRecovery
		);

		double dblDerivativeValue = dblTerminalXVADerivativeValue;
		double dblDerivativeXVAValue = dblTerminalXVADerivativeValue;

		EdgeAssetGreek erug = new EdgeAssetGreek (
			dblDerivativeXVAValue,
			-1.,
			0.,
			dblDerivativeValue
		);

		double dblGainOnBankDefault = -1. * (dblDerivativeXVAValue - maco.bankDefault (dblDerivativeXVAValue));

		double dblGainOnCounterPartyDefault = -1. * (dblDerivativeXVAValue - maco.counterPartyDefault (dblDerivativeXVAValue));

		System.out.println();

		System.out.println ("\t||-----------------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t||                                                    BILATERAL XVA EVOLVER - BURGARD & KJAER (2011) GREEKS EVOLUTION                                                    ||");

		System.out.println ("\t||-----------------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t||    L -> R:                                                                                                                                                            ||");

		System.out.println ("\t||            - Time                                                                                                                                                     ||");

		System.out.println ("\t||            - Derivative XVA Value                                                                                                                                     ||");

		System.out.println ("\t||            - Derivative XVA Value Delta                                                                                                                               ||");

		System.out.println ("\t||            - Derivative XVA Value Gamma                                                                                                                               ||");

		System.out.println ("\t||            - Gain at Bank Default                                                                                                                                     ||");

		System.out.println ("\t||            - Gain at Counter Party Default                                                                                                                            ||");

		System.out.println ("\t||            - Derivative XVA Asset Growth Theta                                                                                                                        ||");

		System.out.println ("\t||            - Derivative XVA Collateral Numeraire Growth Theta                                                                                                         ||");

		System.out.println ("\t||            - Derivative XVA Bank Funding Growth Theta                                                                                                                 ||");

		System.out.println ("\t||            - Derivative XVA Bank Default Growth Theta                                                                                                                 ||");

		System.out.println ("\t||            - Derivative XVA Counter Party Default Growth Theta                                                                                                        ||");

		System.out.println ("\t||            - Derivative XVA Theta Based on Asset Numeraire Down                                                                                                       ||");

		System.out.println ("\t||            - Derivative XVA Theta                                                                                                                                     ||");

		System.out.println ("\t||            - Derivative XVA Theta Based on Asset Numeraire Up                                                                                                         ||");

		System.out.println ("\t||-----------------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t||" +
			FormatUtil.FormatDouble (dblTime, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (erug.derivativeXVAValue(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (erug.derivativeXVAValueDelta(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (erug.derivativeXVAValueGamma(), 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblGainOnBankDefault, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (dblGainOnCounterPartyDefault, 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " | " +
			FormatUtil.FormatDouble (0., 1, 6, 1.) + " ||"
		);

		EdgeEvolutionTrajectory eet = new EdgeEvolutionTrajectory (
			dblTime,
			new UniverseSnapshot (
				meAsset.weinerIncrement (
					new JumpDiffusionVertex (
						dblTime,
						dblDerivativeValue,
						0.,
						false
					),
					dblTimeWidth
				),
				meZeroCouponCollateralBond.weinerIncrement (
					new JumpDiffusionVertex (
						dblTime,
						1.,
						0.,
						false
					),
					dblTimeWidth
				),
				meZeroCouponBankBond.weinerIncrement (
					new JumpDiffusionVertex (
						dblTime,
						1.,
						0.,
						false
					),
					dblTimeWidth
				),
				meZeroCouponCounterPartyBond.weinerIncrement (
					new JumpDiffusionVertex (
						dblTime,
						1.,
						0.,
						false
					),
					dblTimeWidth
				)
			),
			new EdgeReplicationPortfolio (
				1.,
				0.,
				0.,
				0.
			),
			erug,
			dblGainOnBankDefault,
			dblGainOnCounterPartyDefault
		);

		for (dblTime -= dblTimeWidth; dblTime >= 0.; dblTime -= dblTimeWidth)
			eet = RunStep (
				tes,
				si,
				bko,
				eet
			);

		System.out.println ("\t||-----------------------------------------------------------------------------------------------------------------------------------------------------------------------||");

		System.out.println();
	}
}
