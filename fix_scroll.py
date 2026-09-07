import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Make the outer Column scrollable and remove weight
old_outer_column = """            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            )"""

new_outer_column = """            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            )"""
content = content.replace(old_outer_column, new_outer_column)

# Update the Ornate Container Box to remove weight and let it wrap content
old_box = """                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp)
                )"""

new_box = """                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )"""
content = content.replace(old_box, new_box)

# Update internal Column of Ornate container to remove fillMaxSize
old_inner_col = """                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {"""

new_inner_col = """                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                            .drawBehind {"""
content = content.replace(old_inner_col, new_inner_col)

# Replace Spacer weights with explicit heights since we removed fillMaxSize
content = content.replace("Spacer(modifier = Modifier.weight(0.1f))", "Spacer(modifier = Modifier.height(24.dp))")
content = content.replace("Spacer(modifier = Modifier.weight(0.15f))", "Spacer(modifier = Modifier.height(32.dp))")

if "import androidx.compose.foundation.verticalScroll" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.rememberScrollState")

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
