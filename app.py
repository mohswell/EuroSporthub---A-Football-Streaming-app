from flask import Flask, request, jsonify
from pymongo import MongoClient
from dotenv import load_dotenv
import os
import requests

app = Flask(__name__)

# Load environment variables from .env file
load_dotenv()

# MongoDB configuration
MONGO_URI = os.getenv("MONGO_URI")
DB_NAME = os.getenv("DB_NAME")
COLLECTION_NAME = os.getenv("COLLECTION_NAME")
MATCH_COLLECTION = os.getenv("MATCH_COLLECTION_NAME")

# Initialize MongoDB client
client = MongoClient(MONGO_URI)
db = client[DB_NAME]
collection = db[COLLECTION_NAME]
match_collection = db[MATCH_COLLECTION]

# Livescore API configuration
LIVESCORE_API_URL = os.getenv("LIVESCORE_API_URL")
LIVESCORE_API_KEY = os.getenv("LIVESCORE_API_KEY")
LIVESCORE_API_SECRET = os.getenv("LIVESCORE_API_SECRET")
COMPETITION_ID = os.getenv("EURO_COMPETITION_ID")
SEASON = os.getenv("EURO_SEASON")


# Route to fetch teams from Livescore API and store in MongoDB
@app.route('/fetch-teams', methods=['GET'])
def fetch_teams():
    # Livescore API endpoint for getting participants
    teams_url = LIVESCORE_API_URL

    # Parameters for the API request
    params = {
        "key": LIVESCORE_API_KEY,
        "secret": LIVESCORE_API_SECRET,
        "competition_id": COMPETITION_ID,
        "season": SEASON
    }

    # Make a GET request to the API
    response = requests.get(teams_url, params=params)

    # Check if the request was successful
    if response.status_code == 200:
        # Parse the JSON response
        data = response.json()

        # Extract the teams from the response data
        teams = data.get("data", [])

        # Insert teams into the MongoDB collection
        result = collection.insert_many(teams)

        return jsonify({'message': f'{len(result.inserted_ids)} teams fetched and stored successfully.'}), 200
    else:
        return jsonify({'error': 'Failed to fetch teams data from the Livescore API.'}), 500

# Route to fetch the groups from the API and store it in groups collection
@app.route('/fetch-groups', methods=['GET'])
def fetch_groups():
    # Get unique group_id values from the matches collection
    unique_group_ids = match_collection.distinct("group_id")
    
    if not unique_group_ids:
        return jsonify({'error': 'No group IDs found in the matches collection.'}), 500

    for group_id in unique_group_ids:
        url = LIVESCORE_API_URL
        params = {
            "key": LIVESCORE_API_KEY,
            "secret": LIVESCORE_API_SECRET,
            "group_id": group_id
        }
        response = requests.get(url, params=params)
        
        if response.status_code == 200:
            data = response.json()
            
            group_data = data.get("data", {}).get("group", {})
            
            # Insert the entire group data structure
            collection.insert_one(group_data)
        else:
            print(f"Failed to fetch group data for group_id {group_id}")  

    return jsonify({'message': 'Groups data fetched and stored successfully.'}), 200

# Route to get the 24 Countries Flags and store it in a uploads/countryImages folder and country_images collection
@app.route('/country-flags', methods=['GET'])
def fetch_country_flags():
    # Create folder for storing country flag images if it doesn't exist
    folder_path = 'uploads/countryImages'
    
    # Fetch the list of participants
    participants_url = LIVESCORE_API_URL
    participants_params = {
        "key": LIVESCORE_API_KEY,
        "secret": LIVESCORE_API_SECRET,
        "competition_id": COMPETITION_ID,
        "season": SEASON
    }
    participants_response = requests.get(participants_url, params=participants_params)
    
    if participants_response.status_code == 200:
        participants_data = participants_response.json()
        team_ids = [team["id"] for team in participants_data["data"]]
        
        for team_id in team_ids:
            url = f"{LIVESCORE_API_URL}?team_id={team_id}"
            params = {
                "key": LIVESCORE_API_KEY,
                "secret": LIVESCORE_API_SECRET, 
                "competition_id": COMPETITION_ID
            }
            response = requests.get(url, params=params)
            
            if response.status_code == 200:
                image_data = response.content

                # Save the image data to a file
                image_path = os.path.join(folder_path, f"{team_id}.png")
                with open(image_path, 'wb') as f:
                    f.write(image_data)

                # Store the image path in MongoDB
                collection.insert_one({'team_id': team_id, 'image_path': image_path})
            else:
                return jsonify({'error': f'Failed to fetch country flag for team ID {team_id}'}), 500
        
        return jsonify({'message': 'Country flags fetched and stored successfully.'}), 200
    else:
        return jsonify({'error': 'Failed to fetch list of participants'}), 500

# Route to get the match fixtures and store in matches collection
@app.route('/fetch-fixtures', methods=['GET'])
def fetch_fixtures():
    # Livescore API endpoint for getting fixtures
    match_url = LIVESCORE_API_URL

    # Parameters for the API request
    params = {
        "key": LIVESCORE_API_KEY,
        "secret": LIVESCORE_API_SECRET,
        "competition_id": COMPETITION_ID
    }

    # Make a GET request to the API
    response = requests.get(match_url, params=params)

    # Check if the request was successful
    if response.status_code == 200:
        # Parse the JSON response
        data = response.json()

        # Extract the fixtures from the response data
        fixtures = data.get("data", {}).get("fixtures", [])

        # Insert fixtures into the MongoDB collection
        result = collection.insert_many(fixtures)

        return jsonify({'message': f'{len(result.inserted_ids)} match fixtures fetched and stored successfully.'}), 200
    else:
        return jsonify({'error': 'Failed to fetch match fixtures data from the Livescore API.'}), 500

# Route to get the country standings according to UEFA and store it in country_standings collection
@app.route('/fetch-standings', methods=['GET'])
def fetch_country_standings():
    standings_url = LIVESCORE_API_URL
    params = {
        "key": LIVESCORE_API_KEY,
        "secret": LIVESCORE_API_SECRET,
        "competition_id": COMPETITION_ID
    }
    response = requests.get(standings_url, params=params)
    if response.status_code == 200:
        data = response.json()
        
        # Extract the standings from the response data
        standings_data = data.get("data", {}).get("table", [])
        
        if not standings_data:
            return jsonify({'error': 'No country standings data found in the API response.'}), 500
        
        # Insert standings into the MongoDB collection
        collection = db["country_standings"]
        collection.insert_many(standings_data)
        
        return jsonify({'message': 'Country standings data fetched and stored successfully.'}), 200
    else:
        return jsonify({'error': 'Failed to fetch country standings data from the Livescore API.'}), 500


    
# Route to get Head to Head for both teams in the match
# Will need to use match_id for both teams, extract both from matches table and store it head2head collection


# Route to get players in each team and store it in players collection
# Will Implement on June 7 when teams provide their final squads



if __name__ == '__main__':
    app.run(debug=True)