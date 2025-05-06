package com.example.sipscore.utils

import android.content.Context
import com.example.sipscore.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.util.Log
import java.text.SimpleDateFormat
import java.nio.charset.Charset
import java.util.Locale
import java.util.Date

object JsonUtils {

    private fun getJsonFileContent(context: Context, fileName: String): String {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.readText()
        } else {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        }
    }

    fun loadJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            String(buffer, Charset.forName("UTF-8"))
        }
    }

    // --- דירוגים של משתמשים לקפה ---
    fun loadUserRatings(context: Context): MutableMap<String, MutableMap<Int, Int>> {
        return try {
            val json = getJsonFileContent(context, "users.json")
            val type = object : TypeToken<MutableMap<String, MutableMap<Int, Int>>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    fun saveUserRatings(context: Context, data: MutableMap<String, MutableMap<Int, Int>>) {
        val jsonString = Gson().toJson(data)
        val file = File(context.filesDir, "users.json")
        file.writeText(jsonString)
    }

    // --- טעינת סוגי קפה ---
    fun loadCoffeeItems(context: Context): List<CoffeeItem> {
        val json = getJsonFileContent(context, "coffeeTypes.json")
        val type = object : TypeToken<List<CoffeeItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun loadCoffeeTypes(context: Context): List<CoffeeItem> = loadCoffeeItems(context)

    // --- טעינת כל המשתמשים ---
    fun loadUserAccounts(context: Context): Map<String, AccountData> {
        val jsonStr = getJsonFileContent(context, "users.json")
        val jsonArray = JSONArray(jsonStr)
        val users = mutableMapOf<String, AccountData>()

        for (i in 0 until jsonArray.length()) {
            val userObject = jsonArray.getJSONObject(i)
            val email = userObject.keys().next()
            val userData = userObject.getJSONObject(email)

            val firstName = userData.getString("firstName")
            val lastName = userData.getString("lastName")
            val username = userData.getString("username")
            val password = userData.getString("password")
            val profileImage = userData.optString("profileImage", null)

            val ratingsObject = userData.optJSONObject("ratings")
            val ratings = mutableMapOf<String, Double>()
            ratingsObject?.let {
                val keys = it.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    ratings[key] = it.getDouble(key)
                }
            }

            users[email] = AccountData(
                firstName = firstName,
                lastName = lastName,
                username = username,
                password = password,
                profileImage = profileImage,
                ratings = ratings
            )
        }

        return users
    }

    // --- משתמש לפי אימייל ---
    fun loadUserByEmail(context: Context, email: String): AccountData? {
        return loadUserAccounts(context)[email]
    }

    fun saveUserAccounts(context: Context, users: Map<String, AccountData>) {
        val jsonArray = JSONArray()

        for ((email, user) in users) {
            val userData = JSONObject()
            userData.put("firstName", user.firstName)
            userData.put("lastName", user.lastName)
            userData.put("username", user.username)
            userData.put("password", user.password)
            userData.put("profileImage", user.profileImage ?: JSONObject.NULL)

            val ratingsJson = JSONObject()
            for ((coffee, score) in user.ratings) {
                ratingsJson.put(coffee, score)
            }
            userData.put("ratings", ratingsJson)

            val userObject = JSONObject()
            userObject.put(email, userData)
            jsonArray.put(userObject)
        }

        val file = File(context.filesDir, "users.json")
        FileOutputStream(file).use {
            it.write(jsonArray.toString().toByteArray())
        }
    }

    fun loadUserPosts(context: Context, userEmail: String): List<Post> {
        return try {
            val json = getJsonFileContent(context, "posts.json")
            val jsonObject = JSONObject(json)
            val userPostsJsonArray = jsonObject.optJSONArray(userEmail) ?: JSONArray()
            val posts = mutableListOf<Post>()

            for (i in 0 until userPostsJsonArray.length()) {
                val postObject = userPostsJsonArray.getJSONObject(i)
                posts.add(
                    Post(
                        id = postObject.getString("id"),
                        image = postObject.getString("image"),
                        username = postObject.getString("username"),
                        imageProfile = postObject.getString("imageProfile"),
                        content = postObject.getString("content"),
                        date = postObject.getString("date"),
                        email = postObject.getString("email")
                    )
                )
            }

            posts
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveUserPost(context: Context, userEmail: String, post: Post) {
        val fileName = "posts.json"
        val file = File(context.filesDir, fileName)
        val gson = Gson()
        val typeToken = object : TypeToken<MutableMap<String, MutableList<Post>>>() {}.type

        val postsMap: MutableMap<String, MutableList<Post>> =
            if (file.exists()) {
                context.openFileInput(fileName).bufferedReader().use {
                    gson.fromJson(it, typeToken) ?: mutableMapOf()
                }
            } else {
                try {
                    context.assets.open(fileName).bufferedReader().use {
                        gson.fromJson(it, typeToken) ?: mutableMapOf()
                    }
                } catch (e: IOException) {
                    mutableMapOf()
                }
            }

        val userPostsList = postsMap.getOrPut(userEmail) { mutableListOf() }
        userPostsList.add(post)

        context.openFileOutput(fileName, Context.MODE_PRIVATE).bufferedWriter().use {
            gson.toJson(postsMap, it)
        }
    }

    private fun loadAllPosts(context: Context): Map<String, List<Post>> {
        return try {
            val json = getJsonFileContent(context, "posts.json")
            val jsonObject = JSONObject(json)
            val result = mutableMapOf<String, List<Post>>()

            for (key in jsonObject.keys()) {
                val postsArray = jsonObject.getJSONArray(key)
                val postList = mutableListOf<Post>()
                for (i in 0 until postsArray.length()) {
                    val postObj = postsArray.getJSONObject(i)
                    postList.add(
                        Post(
                            id = postObj.getString("id"),
                            image = postObj.getString("image"),
                            username = postObj.getString("username"),
                            imageProfile = postObj.getString("imageProfile"),
                            content = postObj.getString("content"),
                            date = postObj.getString("date"),
                            email = postObj.getString("email")
                        )
                    )
                }
                result[key] = postList
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    fun generateNotificationsForUser(context: Context, userEmail: String): List<NotificationItem> {
        val postsMap = loadAllPosts(context)
        val usersMap = loadUserAccounts(context)
        val notifications = mutableListOf<NotificationItem>()

        for ((posterEmail, postList) in postsMap) {
            for (post in postList) {
                val likesArray = post.likes
                if (likesArray.contains(userEmail)) {
                    val likerData = usersMap[post.email]
                    if (likerData != null) {
                        val safeDate = post.date?.takeIf { it.isNotBlank() }
                            ?: SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

                        notifications.add(
                            NotificationItem(
                                likerUsername = likerData.username,
                                likerProfileImage = likerData.profileImage ?: "",
                                postImage = post.image,
                                message = "${likerData.username} liked your post",
                                date = safeDate
                            )
                        )
                    }
                }
            }
        }
        return notifications
    }

//    fun loadUserFromJson(context: Context, email: String): AccountData? {
//        return try {
//            val json = loadJSONFromAsset(context, "users.json") // טוען את הקובץ
//            val jsonObject = JSONObject(json)
//
//            if (!jsonObject.has(email)) {
//                Log.w("UserLoad", "Email not found: $email")
//                return null
//            }
//
//            val userObj = jsonObject.getJSONObject(email)
//
//            val firstName = userObj.optString("firstName", "")
//            val lastName = userObj.optString("lastName", "")
//            val username = userObj.optString("username", "")
//            val password = userObj.optString("password", "")
//            val profileImage = userObj.optString("profileImage", "")
//
//            val ratings = mutableMapOf<String, Double>()
//            val ratingsObj = userObj.optJSONObject("ratings")
//            if (ratingsObj != null) {
//                val keys = ratingsObj.keys()
//                while (keys.hasNext()) {
//                    val coffeeName = keys.next()
//                    val rating = ratingsObj.optDouble(coffeeName, -1.0)
//                    if (rating >= 0) {
//                        ratings[coffeeName] = rating
//                    }
//                }
//            }
//
//            AccountData(
//                email = email,
//                firstName = firstName,
//                lastName = lastName,
//                username = username,
//                profileImage = profileImage,
//                ratings = ratings
//            )
//        } catch (e: Exception) {
//            Log.e("UserLoad", "Error loading user: ${e.message}", e)
//            null
//        }
//    }

    fun loadUserFromJson(context: Context, email: String): AccountData? {
        return try {
            val json = loadJsonFromAssets(context, "users.json") // שם הפונקציה המדויק אצלך
            val jsonArray = JSONArray(json)

            for (i in 0 until jsonArray.length()) {
                val userObject = jsonArray.getJSONObject(i)
                val currentEmail = userObject.keys().next()
                if (currentEmail == email) {
                    val userData = userObject.getJSONObject(currentEmail)

                    val firstName = userData.optString("firstName", "")
                    val lastName = userData.optString("lastName", "")
                    val username = userData.optString("username", "")
                    val password = userData.optString("password", "")
                    val profileImage = userData.optString("profileImage", "")

                    val ratings = mutableMapOf<String, Double>()
                    val ratingsObj = userData.optJSONObject("ratings")
                    if (ratingsObj != null) {
                        val keys = ratingsObj.keys()
                        while (keys.hasNext()) {
                            val coffeeName = keys.next()
                            val rating = ratingsObj.optDouble(coffeeName, -1.0)
                            if (rating >= 0) {
                                ratings[coffeeName] = rating
                            }
                        }
                    }

                    return AccountData(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        password = password,
                        profileImage = profileImage,
                        ratings = ratings
                    )
                }
            }

            Log.w("UserLoad", "Email not found: $email")
            null
        } catch (e: Exception) {
            Log.e("UserLoad", "Error loading user: ${e.message}", e)
            null
        }
    }

}
