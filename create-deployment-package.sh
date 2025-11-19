#!/bin/bash

# Create Deployment Packages for Manual Console Upload
# This script builds your application and creates ZIP files ready for AWS Console upload

set -e

echo "📦 Creating Deployment Packages for Manual Upload"
echo "=================================================="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Clean and build
echo ""
echo "🔨 Building application..."
./gradlew clean bootJar -x test

if [ ! -f "build/libs/kelox-be-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}❌ Build failed - JAR not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Build successful${NC}"

# Create output directory
OUTPUT_DIR="deployment-packages"
mkdir -p "$OUTPUT_DIR"

# Get JAR file name
JAR_FILE="build/libs/kelox-be-0.0.1-SNAPSHOT.jar"
JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)

echo ""
echo "📦 Creating deployment packages..."

# Create dev package
echo ""
echo "Creating DEV package..."
cd build/libs
zip -q ../../$OUTPUT_DIR/kelox-backend-dev.zip kelox-be-0.0.1-SNAPSHOT.jar
cd ../..

# Add dev .ebextensions if they exist
if [ -d ".ebextensions-dev" ]; then
    cd .ebextensions-dev
    zip -q -r ../$OUTPUT_DIR/kelox-backend-dev.zip .
    cd ..
fi

echo -e "${GREEN}✓ DEV package created${NC}"

# Create prod package
echo "Creating PROD package..."
cd build/libs
zip -q ../../$OUTPUT_DIR/kelox-backend-prod.zip kelox-be-0.0.1-SNAPSHOT.jar
cd ../..

# Add prod .ebextensions if they exist
if [ -d ".ebextensions" ]; then
    cd .ebextensions
    zip -q -r ../$OUTPUT_DIR/kelox-backend-prod.zip .
    cd ..
fi

echo -e "${GREEN}✓ PROD package created${NC}"

# Also copy standalone JAR
cp "$JAR_FILE" "$OUTPUT_DIR/kelox-backend-standalone.jar"

# Show results
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✅ Deployment Packages Created!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📁 Output directory: $OUTPUT_DIR/"
echo ""
ls -lh "$OUTPUT_DIR"
echo ""

# Calculate sizes
DEV_SIZE=$(du -h "$OUTPUT_DIR/kelox-backend-dev.zip" | cut -f1)
PROD_SIZE=$(du -h "$OUTPUT_DIR/kelox-backend-prod.zip" | cut -f1)
STANDALONE_SIZE=$(du -h "$OUTPUT_DIR/kelox-backend-standalone.jar" | cut -f1)

echo "📊 Package Details:"
echo "─────────────────────────────────────────"
echo "  DEV package:        kelox-backend-dev.zip ($DEV_SIZE)"
echo "    └─ Includes: JAR + dev configuration"
echo ""
echo "  PROD package:       kelox-backend-prod.zip ($PROD_SIZE)"
echo "    └─ Includes: JAR + production configuration"
echo ""
echo "  Standalone JAR:     kelox-backend-standalone.jar ($STANDALONE_SIZE)"
echo "    └─ JAR only, no configuration"
echo ""

echo "🚀 How to Deploy:"
echo "─────────────────────────────────────────"
echo "1. Go to AWS Elastic Beanstalk Console"
echo "2. Select your environment (or create new)"
echo "3. Click 'Upload and deploy'"
echo "4. Choose file:"
echo "   - For dev:  $OUTPUT_DIR/kelox-backend-dev.zip"
echo "   - For prod: $OUTPUT_DIR/kelox-backend-prod.zip"
echo "   - Simple:   $OUTPUT_DIR/kelox-backend-standalone.jar"
echo "5. Enter version label (e.g., v1.0)"
echo "6. Click 'Deploy'"
echo ""

echo "📝 Don't forget to set environment variables!"
echo "─────────────────────────────────────────"
echo "In AWS Console: EB > Configuration > Software > Environment properties"
echo ""
echo "Required variables:"
echo "  SPRING_PROFILES_ACTIVE  = dev (or prod)"
echo "  SERVER_PORT            = 5000"
echo "  DB_URL                 = jdbc:postgresql://[endpoint]:5432/kelox_db"
echo "  DB_USERNAME            = postgres"
echo "  DB_PASSWORD            = [your-password]"
echo "  ADMIN_SECRET_CODE      = [secret]"
echo "  JWT_SECRET             = [secret]"
echo ""
echo "📚 See MANUAL_CONSOLE_DEPLOYMENT.md for complete guide"
echo ""

