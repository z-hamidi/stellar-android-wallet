package blockeq.com.stellarwallet.adapters

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.interfaces.OnImageLoaded
import blockeq.com.stellarwallet.models.SupportedAsset
import blockeq.com.stellarwallet.services.networking.SupportedAssetsApi
import blockeq.com.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces
import org.stellar.sdk.responses.AccountResponse
import java.util.*

class AssetsRecyclerViewAdapter(var context: Context, var items : ArrayList<Any>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_ASSET = 0
        const val TYPE_HEADER = 1
        const val TYPE_SUPPORTED_ASSET = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType) {
            TYPE_ASSET, TYPE_SUPPORTED_ASSET -> {
                val v = inflater.inflate(R.layout.item_asset, parent, false)
                AssetViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_asset_header, parent, false)
                AssetHeaderViewHolder(v)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            items[position] is AccountResponse.Balance -> TYPE_ASSET
            items[position] is SupportedAsset -> TYPE_SUPPORTED_ASSET
            else -> TYPE_HEADER
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_ASSET -> {
                val vh = holder as AssetViewHolder
                configureAssetViewHolder(vh, position)
            }
            TYPE_SUPPORTED_ASSET -> {
                val vh = holder as SupportedAssetViewHolder
                configureSupportedAssetViewHolder(vh, position)
            }
            else -> {
                val vh = holder as AssetHeaderViewHolder
                configureAssetHeaderViewHolder(vh, position)
            }
        }
    }

    //region View Holders

    class AssetViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var assetImage : ImageView? = null
        var assetName : TextView? = null
        var assetAmount : TextView? = null
        var assetButton : Button? = null

        init {
            assetImage = v.findViewById(R.id.assetImageView)
            assetName = v.findViewById(R.id.assetNameTextView)
            assetAmount = v.findViewById(R.id.assetAmountTextView)
            assetButton = v.findViewById(R.id.assetButton)
        }
    }

    class AssetHeaderViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var title : TextView? = null

        init {
            title = v.findViewById(R.id.titleText)
        }
    }

    class SupportedAssetViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var assetImage : ImageView? = null
        var assetName : TextView? = null
        var assetAmount : TextView? = null
        var assetButton : Button? = null

        init {
            assetImage = v.findViewById(R.id.assetImageView)
            assetName = v.findViewById(R.id.assetNameTextView)
            assetAmount = v.findViewById(R.id.assetAmountTextView)
            assetButton = v.findViewById(R.id.assetButton)
        }
    }

    //endregion

    //region Bind View Holders

    private fun configureAssetViewHolder(viewHolder : AssetViewHolder, position : Int) {
        val asset = items[position] as AccountResponse.Balance

        var assetCode = ""

        if (asset.assetType == Constants.LUMENS_ASSET_TYPE) {
            viewHolder.assetName!!.text = Constants.LUMENS_ASSET_NAME
            assetCode = "XLM"
        } else {
            when(asset.assetCode) {
                Constants.PTS_ASSET_TYPE -> {
                    viewHolder.assetName!!.text = Constants.PTS_ASSET_NAME
                }
            }
            assetCode = asset.assetCode
        }

        viewHolder.assetAmount!!.text = truncateDecimalPlaces(asset.balance) + " " + assetCode
    }

    private fun configureAssetHeaderViewHolder(viewHolder : AssetHeaderViewHolder, position : Int) {
        val titleText = items[position] as String
        viewHolder.title!!.text = titleText
    }

    private fun configureSupportedAssetViewHolder(viewHolder: SupportedAssetViewHolder, position: Int) {
        val asset = items[position] as SupportedAsset

        viewHolder.assetName!!.text = asset.name + " (" + asset.code + ")"

        val listener = object: OnImageLoaded {
            override fun onImageLoaded(bitmap: Bitmap) {
                viewHolder.assetImage!!.setImageBitmap(bitmap)
            }

            override fun onLoadedError(e: Exception) {
                Toast.makeText(context, "There was an error loading assets", Toast.LENGTH_SHORT).show()
            }

        }

        SupportedAssetsApi.Companion.DownloadImageTask(listener).execute()
    }

    //endregion
}
