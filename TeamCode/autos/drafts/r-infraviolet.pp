{
  "version": "2.0.0",
  "timestamp": "2026-11-06T13:04:21.706Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "headingDeg": 90,
    "locked": false
  },
  "lines": [
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-flower",
      "name": "wall flower to WALL_FLOWER_STAGING",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-lane-north",
      "name": "wall lane north to LEFT_PAIR_OFFSET",
      "endPoint": {
        "x": 34,
        "y": 124
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 72
        },
        {
          "x": 30,
          "y": 104
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 303.161
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-lane-south",
      "name": "wall lane south to GARDEN_STAGING",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 102
        },
        {
          "x": 30,
          "y": 48
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.22,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 303.161,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.22,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-right-offset",
      "name": "right offset to RIGHT_PAIR_OFFSET",
      "endPoint": {
        "x": 32,
        "y": 20
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 57.714
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-near-park",
      "name": "near park to RIGHT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 33,
          "y": 43
        },
        {
          "x": 32,
          "y": 78
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 57.714,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.7,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            }
          ]
        }
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "id": "r-link-left-abort-park",
      "name": "Link to LEFT_PAIR_OFFSET",
      "endPoint": {
        "x": 34,
        "y": 124
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 303.161
      },
      "locked": false
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-left-abort-park",
      "name": "left abort park to RIGHT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 113
        },
        {
          "x": 32,
          "y": 98
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 303.161,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.6,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 0
              }
            }
          ]
        }
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "id": "r-link-wall-abort-park",
      "name": "Link to WALL_FLOWER_STAGING",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 180
      },
      "locked": false
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-wall-abort-park",
      "name": "wall abort park to RIGHT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 31,
          "y": 59
        },
        {
          "x": 31,
          "y": 81
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      },
      "color": "#e15759"
    },
    {
      "kind": "atomic",
      "id": "r-link-garden-abort-park",
      "name": "Link to GARDEN_STAGING",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 270
      },
      "locked": false
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "r-garden-abort-park",
      "name": "garden abort park to RIGHT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 95.7
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 35
        },
        {
          "x": 31,
          "y": 77
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 0
      },
      "color": "#e15759"
    }
  ],
  "shapes": [
    {
      "id": "biobuzz-red-hive",
      "name": "Red HIVE",
      "vertices": [
        {
          "x": 46,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 91.4
        },
        {
          "x": 46,
          "y": 91.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-blue-hive",
      "name": "Blue HIVE",
      "vertices": [
        {
          "x": 96.5,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 50
        },
        {
          "x": 96.5,
          "y": 50
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-top",
      "name": "Scoring-Side FLOWER",
      "vertices": [
        {
          "x": 43.4,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 139.4
        },
        {
          "x": 46.75,
          "y": 137.7
        },
        {
          "x": 43.4,
          "y": 139.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-flower-right",
      "name": "Blue Alliance FLOWER",
      "vertices": [
        {
          "x": 141.5,
          "y": 98.1
        },
        {
          "x": 139.4,
          "y": 98.1
        },
        {
          "x": 137.7,
          "y": 94.75
        },
        {
          "x": 139.4,
          "y": 91.4
        },
        {
          "x": 141.5,
          "y": 91.4
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-bottom",
      "name": "Audience-Side FLOWER",
      "vertices": [
        {
          "x": 91.15,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 2.1
        },
        {
          "x": 94.5,
          "y": 3.8
        },
        {
          "x": 91.15,
          "y": 2.1
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-left",
      "name": "Red Alliance FLOWER",
      "vertices": [
        {
          "x": 0,
          "y": 50.35
        },
        {
          "x": 2.1,
          "y": 50.35
        },
        {
          "x": 3.8,
          "y": 47
        },
        {
          "x": 2.1,
          "y": 43.65
        },
        {
          "x": 0,
          "y": 43.65
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    }
  ],
  "sequence": [],
  "fieldPoints": [],
  "robot": {
    "forwardVelocity": 75,
    "strafeVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rLength": 15.44,
    "rWidth": 16,
    "rForward": 0,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 50,
    "maxDeceleration": 50,
    "features": [
      {
        "name": "FLOWER poker",
        "length": 1.5,
        "width": 1.5,
        "forward": 8.5,
        "left": 0,
        "collides": true
      }
    ]
  },
  "activePaths": [],
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "registry": {
      "actions": [
        "LaunchAll",
        "LaunchOne",
        "SpinUp",
        "PickFlower",
        "GardenGather"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "CellUp",
        "Tip",
        "CameraBlind"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4.5,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 3.7,
        "GardenGather": 5.0
      }
    },
    "points": {
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "LEFT_PAIR_OFFSET": [
        34,
        124,
        303.161
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_PAIR_OFFSET": [
        32,
        20,
        57.714
      ],
      "RIGHT_PARK": [
        14.5,
        95.7,
        0
      ]
    },
    "pathEnds": {
      "r-wall-flower": "WALL_FLOWER_STAGING",
      "r-wall-lane-north": "LEFT_PAIR_OFFSET",
      "r-wall-lane-south": "GARDEN_STAGING",
      "r-right-offset": "RIGHT_PAIR_OFFSET",
      "r-near-park": "RIGHT_PARK",
      "r-left-abort-park": "RIGHT_PARK",
      "r-wall-abort-park": "RIGHT_PARK",
      "r-garden-abort-park": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "R-action-5",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "R-action-6",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "card-r-wall-flower",
        "kind": "path",
        "lineId": "r-wall-flower",
        "park": false
      },
      {
        "id": "R-action-7",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "card-r-wall-lane-north",
        "kind": "path",
        "lineId": "r-wall-lane-north",
        "park": false
      },
      {
        "id": "R-wait-9",
        "kind": "firstOf",
        "label": "LEFT up before 14 s, otherwise park",
        "rows": [
          {
            "afterMs": 14000,
            "matchClock": true,
            "cards": [
              {
                "id": "card-r-left-abort-park",
                "kind": "path",
                "lineId": "r-left-abort-park",
                "park": true
              }
            ]
          },
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "R-action-8",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "card-r-wall-lane-south",
                "kind": "path",
                "lineId": "r-wall-lane-south",
                "park": false
              },
              {
                "id": "R-wait-4",
                "kind": "firstOf",
                "label": "RIGHT up by 19 s: reload or park",
                "rows": [
                  {
                    "afterMs": 19000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "card-r-garden-abort-park",
                        "kind": "path",
                        "lineId": "r-garden-abort-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "R-action-3",
                        "kind": "action",
                        "name": "GardenGather",
                        "timeoutS": 5.5
                      },
                      {
                        "id": "card-r-right-offset",
                        "kind": "path",
                        "lineId": "r-right-offset",
                        "park": false
                      },
                      {
                        "id": "R-wait-2",
                        "kind": "firstOf",
                        "label": "RIGHT up before 22.5 s, otherwise park",
                        "rows": [
                          {
                            "afterMs": 22500,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "card-r-near-park",
                                "kind": "path",
                                "lineId": "r-near-park",
                                "park": true
                              }
                            ]
                          },
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": [
                              {
                                "id": "R-action-1",
                                "kind": "action",
                                "name": "LaunchAll",
                                "timeoutS": 1
                              },
                              {
                                "id": "card-r-near-park-alt",
                                "kind": "path",
                                "lineId": "r-near-park",
                                "park": true
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "exportName": "R-Infraviolet",
    "parkFrom": {
      "card-r-wall-flower": "r-wall-abort-park",
      "card-r-wall-lane-north": "r-left-abort-park",
      "card-r-wall-lane-south": "r-garden-abort-park",
      "card-r-right-offset": "r-near-park"
    }
  }
}
