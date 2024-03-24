package it.torino.mobin.onboarding.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import it.torino.mobin.R
import it.torino.mobin.ui.theme.DoubleSpacerHeight
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.SmallPadding
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.mobin.ui.theme.marginVertical
import it.torino.mobin.utils.CustomButton
import it.torino.mobin.utils.LocalPreferencesManager
import it.torino.mobin.utils.PreferencesManager

@Composable
fun PrivacyPolicy(navController: NavHostController) {
    val context = LocalContext.current
    val preferencesManager = LocalPreferencesManager.current
    val myPreferenceKey = LocalContext.current.getString(R.string.my_preference_key)
    val isPreferenceEnabled = preferencesManager.getBoolean(myPreferenceKey)
    var acceptedTCs by remember { mutableStateOf(false) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(top=SmallPadding)
    ) {
        val (title, card, tcs, button) = createRefs()
        Text(
            LocalContext.current.getString(R.string.participant_information_and_policy),
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacerHeight)
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(MediumPadding)
                .constrainAs(card) {
                    top.linkTo(title.bottom)
                    start.linkTo(title.start)
                    end.linkTo(parent.end)
                    height = Dimension.percent(0.6f)
                },
            shape = RoundedCornerShape(size = 20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            val numbers = (0..10).toList()
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = marginVertical)
            ) {
                itemsIndexed(numbers) { index, item ->
                    when (item) {
                        0 -> Text(
                            LocalContext.current.getString(R.string.privacy_policy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MediumPadding)
                                .clickable {
                                    openUrl(
                                        context,
                                        context.getString(R.string.privacy_policy_link_en)
                                    )
                                },
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center,
                                textDecoration = TextDecoration.Underline),
                        )

                        else -> Text(
                            DynamicStringResourceBuilder(item),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MediumPadding),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        Row(modifier=Modifier.fillMaxWidth()
            .padding(start= MediumPadding)
            .constrainAs(tcs) {
                top.linkTo(card.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.percent(0.1f)
            },
            verticalAlignment= Alignment.CenterVertically) {
            Text(text = context.getString(R.string.accept_privacy))
            Checkbox(
                checked = acceptedTCs,
                onCheckedChange = { checkStatus ->
                    acceptedTCs = checkStatus
                    preferencesManager.setBoolean(myPreferenceKey, checkStatus)
                    navController.popBackStack()
                }
            )
        }
        val modifier = Modifier.constrainAs(button) {
            top.linkTo(tcs.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            height = Dimension.percent(0.1f)
        }
        CustomButton(modifier, context.getString(R.string.next),
                contentColour = if (acceptedTCs) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                containerColour = if (acceptedTCs) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ) {
            if (acceptedTCs)
                navController.navigate("T&Cs") {
                    setPrivacyPolicyShown(context, preferencesManager)
                    navController.popBackStack()
                }
        }
    }
}

/**
 * it opens a link provided as input
 */
fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    context.startActivity(intent)
}
fun getStringResourceByName(context: Context, resourceName: String): String {
    val resourceId = context.resources.getIdentifier(resourceName, "string", context.packageName)
    return if (resourceId == 0) {
        "Resource not found"
    } else {
        context.getString(resourceId)
    }
}

// Usage within a Composable
@Composable
fun DynamicStringResourceBuilder(index: Int): String {
    val context = LocalContext.current
    val resourceName = "privacy_policy_$index"
    val privacyPolicyString = getStringResourceByName(context, resourceName)

    return privacyPolicyString
}
@Composable
fun privacyPolicyShown(preferencesManager: PreferencesManager): Boolean {
    val myPreferenceKey = LocalContext.current.getString(R.string.policy_shown_key)
    // Check and react to the permission state
    return preferencesManager.getBoolean(myPreferenceKey)
}
fun setPrivacyPolicyShown(context:Context, preferencesManager: PreferencesManager) {
    val myPreferenceKey = context.getString(R.string.policy_shown_key)
    // Check and react to the permission state
    return preferencesManager.setBoolean(myPreferenceKey, true)
}
