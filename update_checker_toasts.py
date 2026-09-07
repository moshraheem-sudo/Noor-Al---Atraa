import re

with open('app/src/main/java/com/example/UpdateChecker.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                    if (cleanTag.isNotEmpty() && isNewerVersion(cleanTag, cleanCurrent)) {
                        val assets = jsonObj.optJSONArray("assets")
                        if (assets != null && assets.length() > 0) {
                            val apkUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                            if (apkUrl.isNotEmpty() && apkUrl.endsWith(".apk")) {
                                withContext(Dispatchers.Main) {
                                    showUpdateDialog(context, tagName, apkUrl)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle failures silently as requested
                e.printStackTrace()
            }"""

replacement = """                    if (cleanTag.isNotEmpty() && isNewerVersion(cleanTag, cleanCurrent)) {
                        val assets = jsonObj.optJSONArray("assets")
                        if (assets != null && assets.length() > 0) {
                            val apkUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                            if (apkUrl.isNotEmpty() && apkUrl.endsWith(".apk")) {
                                withContext(Dispatchers.Main) {
                                    showUpdateDialog(context, tagName, apkUrl)
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "أنت تمتلك أحدث نسخة من التطبيق.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "تعذر التحقق من التحديثات.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حدث خطأ أثناء الاتصال بالخادم.", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }"""

new_content = content.replace(target, replacement)

with open('app/src/main/java/com/example/UpdateChecker.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Done")
