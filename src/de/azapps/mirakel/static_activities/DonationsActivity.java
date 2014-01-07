/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 * 
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.static_activities;

import org.sufficientlysecure.donations.DonationsFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import de.azapps.mirakel.helper.BuildHelper;
import de.azapps.mirakelandroid.R;

public class DonationsActivity extends FragmentActivity {

	/**
	 * Flattr
	 */
	private static final String		FLATTR_PROJECT_URL		= "http://mirakel.azapps.de/";
	// FLATTR_URL without http:// !
	private static final String		FLATTR_URL				= "flattr.com/thing/2188714";

	private static final String[]	GOOGLE_CATALOG			= new String[] {
		"mirakel.donation.50", "mirakel.donation.100",
		"mirakel.donation.200", "mirakel.donation.500",
		"mirakel.donation.1000", "mirakel.donation.1500",
		"mirakel.donation.2500", "mirakel.donation.19900", };
	/**
	 * Google
	 */
	private static final String		GOOGLE_PUBKEY			= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmjcA2Hmr/HVH5raLa6RMTbY/n5QbhqnGOvcLCVQqxj+A4N2vWke7N0Y2tvSS8LYvpdSt5INHtyl1DNaJ/42fdMoFnwLO9lEYvQ1AMPBPt7BtBm2qw/L4hybqYCg/nyzZ2GI/Te6pDgHBUxcaIR0b8IRFwc+3lZHCIxIqq7VjEcxV6hgbNC5Tx5Lt69eTDvZIPwIjU0h/hVDUNxZxWEOGpWRfSqCtTQWSA8Vo8ssAK7n/s8NtpAGn84ZJWFF8SyZc0Y3jjCb9FCRgF7D6xXLPbl1O6ekLIU6zG4RqaaxqymHiXpkq9cYmV/9A3RJathc9WyvPlj7oRlCYo12vmqIV+QIDAQAB";

	private static final String		PAYPAL_CURRENCY_CODE	= "EUR";
	/**
	 * PayPal
	 */
	private static final String		PAYPAL_USER				= "anatolij.z@web.de";

	/**
	 * Needed for Google Play In-app Billing. It uses startIntentSenderForResult(). The result is
	 * not propagated to
	 * the Fragment like in startActivityForResult(). Thus we need to propagate manually to our
	 * Fragment.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager
				.findFragmentByTag("donationsFragment");
		if (fragment != null) {
			((DonationsFragment) fragment).onActivityResult(requestCode,
					resultCode, data);
		}
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.donations_activity);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		DonationsFragment donationsFragment;
		if (BuildHelper.isForPlayStore()) {
			donationsFragment = DonationsFragment.newInstance(
					BuildHelper.isDebug(),
					true,
					GOOGLE_PUBKEY,
					GOOGLE_CATALOG,
					getResources().getStringArray(
							R.array.donation_google_catalog_values), false,
							null, null, null, false, FLATTR_PROJECT_URL, FLATTR_URL);
		} else {
			donationsFragment = DonationsFragment.newInstance(
					BuildHelper.isDebug(), false, null, null, null, true,
					PAYPAL_USER, PAYPAL_CURRENCY_CODE,
					getString(R.string.donation_paypal_item), true,
					FLATTR_PROJECT_URL, FLATTR_URL);
		}

		ft.replace(R.id.donations_activity_container, donationsFragment,
				"donationsFragment");
		ft.commit();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(RESULT_OK);
				finish();
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
